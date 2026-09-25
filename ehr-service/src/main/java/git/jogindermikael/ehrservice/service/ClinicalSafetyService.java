package git.jogindermikael.ehrservice.service;

import git.jogindermikael.ehrservice.dto.Alert;
import git.jogindermikael.ehrservice.dto.Rule;
import git.jogindermikael.ehrservice.repository.EhrRepository;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ClinicalSafetyService {
  private final git.jogindermikael.ehrservice.integration.WorkflowClient downstream;
  private final JdbcTemplate jdbc;
  private final EhrRepository repository;

  public ClinicalSafetyService(
      JdbcTemplate jdbc,
      EhrRepository repository,
      git.jogindermikael.ehrservice.integration.WorkflowClient downstream) {
    this.downstream = downstream;
    this.jdbc = jdbc;
    this.repository = repository;
  }

  public void lock() {
    jdbc.queryForObject("SELECT id FROM clinical_safety_lock WHERE id=1 FOR UPDATE", Integer.class);
  }

  public void check(UUID patient, String medication) {
    lock();
    String drug = medication.strip().toLowerCase(Locale.ROOT);
    boolean allergy =
        repository.findHistories().stream()
            .filter(h -> h.patientId().equals(patient))
            .anyMatch(
                h ->
                    h.allergies() != null
                        && h.allergies().stream().anyMatch(a -> a.strip().equalsIgnoreCase(drug)));
    if (allergy)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Medication matches a recorded allergy");
    var active =
        repository.findPrescriptions().stream()
            .filter(p -> p.patientId().equals(patient) && "ACTIVE".equals(p.status()))
            .toList();
    for (var p : active) {
      String other = p.medication().strip().toLowerCase(Locale.ROOT);
      if (other.equals(drug))
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate active medication");
      if (jdbc.queryForObject(
              "SELECT COUNT(*) FROM medication_safety_rule WHERE (medication=? AND"
                  + " interacting_medication=?) OR (medication=? AND interacting_medication=?)",
              Integer.class,
              drug,
              other,
              other,
              drug)
          > 0)
        throw new ResponseStatusException(
            HttpStatus.CONFLICT, "Configured medication interaction blocks prescribing");
    }
  }

  public UUID rule(Rule c) {
    lock();
    UUID id = UUID.randomUUID();
    jdbc.update(
        "INSERT INTO medication_safety_rule VALUES (?,?,?,?)",
        id,
        c.medication().strip().toLowerCase(Locale.ROOT),
        c.interactingMedication().strip().toLowerCase(Locale.ROOT),
        c.reason());
    return id;
  }

  public Map<String, Object> alert(Alert c) {
    lock();
    var old =
        jdbc.queryForList(
            "SELECT * FROM clinical_alert WHERE source_reference=?", c.sourceReference());
    if (!old.isEmpty()) {
      var row = old.getFirst();
      if (!c.patientId().equals(row.get("patient_id"))
          || !c.assigneeId().equals(row.get("assignee_id"))
          || !c.summary().equals(row.get("summary")))
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Alert reference reused");
      return row;
    }
    UUID id = UUID.randomUUID();
    jdbc.update(
        "INSERT INTO clinical_alert VALUES (?,?,?,?,?,'OPEN',CURRENT_TIMESTAMP,NULL,NULL)",
        id,
        c.patientId(),
        c.assigneeId(),
        c.sourceReference(),
        c.summary());
    return jdbc.queryForMap("SELECT * FROM clinical_alert WHERE id=?", id);
  }

  public List<Map<String, Object>> alerts(UUID patient) {
    return jdbc.queryForList(
        "SELECT * FROM clinical_alert WHERE patient_id=? ORDER BY created_at", patient);
  }

  public void acknowledge(UUID id) {
    var auth =
        org.springframework.security.core.context.SecurityContextHolder.getContext()
            .getAuthentication();
    if (auth == null)
      throw new org.springframework.security.access.AccessDeniedException("Identity required");
    var rows = jdbc.queryForList("SELECT * FROM clinical_alert WHERE id=? FOR UPDATE", id);
    if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found");
    var row = rows.getFirst();
    if (!auth.getName().equals(row.get("assignee_id").toString())
        && auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
      throw new org.springframework.security.access.AccessDeniedException(
          "Assigned clinician required");
    jdbc.update(
        "UPDATE clinical_alert SET"
            + " status='ACKNOWLEDGED',acknowledged_by=?,acknowledged_at=CURRENT_TIMESTAMP WHERE"
            + " id=? AND status='OPEN'",
        auth.getName(),
        id);
  }

  public Map<String, Object> escalate(UUID id) {
    var row =
        jdbc.queryForList("SELECT * FROM clinical_alert WHERE id=?", id).stream()
            .findFirst()
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found"));
    if (!"OPEN".equals(row.get("status")))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Alert already acknowledged");
    return downstream.task(
        Map.of(
            "reference",
            "ehr-alert:" + id,
            "patientId",
            row.get("patient_id"),
            "assigneeId",
            row.get("assignee_id"),
            "title",
            row.get("summary")));
  }

  public Map<String, Object> dispensingSafety(UUID id) {
    lock();
    var prescription =
        repository.findPrescriptions().stream()
            .filter(p -> p.id().equals(id))
            .findFirst()
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "EHR prescription not found"));
    if (!"ACTIVE".equals(prescription.status()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "EHR prescription is not active");
    String drug = prescription.medication().strip().toLowerCase(Locale.ROOT);
    boolean allergy =
        repository.findHistories().stream()
            .filter(h -> h.patientId().equals(prescription.patientId()))
            .anyMatch(
                h ->
                    h.allergies() != null
                        && h.allergies().stream().anyMatch(a -> a.strip().equalsIgnoreCase(drug)));
    if (allergy)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Recorded allergy blocks dispensing");
    for (var other : repository.findPrescriptions())
      if (other.patientId().equals(prescription.patientId())
          && !other.id().equals(id)
          && "ACTIVE".equals(other.status())) {
        String second = other.medication().strip().toLowerCase(Locale.ROOT);
        if (drug.equals(second)
            || jdbc.queryForObject(
                    "SELECT COUNT(*) FROM medication_safety_rule WHERE (medication=? AND"
                        + " interacting_medication=?) OR (medication=? AND"
                        + " interacting_medication=?)",
                    Integer.class,
                    drug,
                    second,
                    second,
                    drug)
                > 0)
          throw new ResponseStatusException(
              HttpStatus.CONFLICT, "Medication safety conflict blocks dispensing");
      }
    return Map.of(
        "id",
        id,
        "patientId",
        prescription.patientId(),
        "medication",
        prescription.medication(),
        "status",
        prescription.status());
  }

  public void discontinue(UUID id) {
    lock();
    if (jdbc.update(
            "UPDATE prescription SET status='DISCONTINUED' WHERE id=? AND status='ACTIVE'", id)
        != 1)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Active prescription not found");
  }
}
