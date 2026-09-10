package git.jogindermikael.patientportalservice.service;

import git.jogindermikael.patientportalservice.repository.PatientPortalRepository;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PortalWorkflowService {
  private final git.jogindermikael.patientportalservice.integration.WorkflowClient downstream;
  private final JdbcTemplate jdbc;
  private final PortalAccess access;
  private final PatientPortalRepository repository;
  private final IdentityDirectoryClient identities;

  public PortalWorkflowService(
      JdbcTemplate jdbc,
      PortalAccess access,
      PatientPortalRepository repository,
      git.jogindermikael.patientportalservice.integration.WorkflowClient downstream,
      IdentityDirectoryClient identities) {
    this.downstream = downstream;
    this.jdbc = jdbc;
    this.access = access;
    this.repository = repository;
    this.identities = identities;
  }

  public record Identity(@NotBlank @Size(max = 200) String subject, @NotNull UUID patientId) {
  }

  public void bind(Identity c) {
    if (!access.admin())
      throw new org.springframework.security.access.AccessDeniedException("Admin required");
    identities.validatePatientIdentity(c.subject(), c.patientId());
    repository.lock();
    jdbc.update(
        "INSERT INTO portal_identity(subject,patient_id,status,patient_status) VALUES (?,?,?,?)",
        c.subject(), c.patientId(), "ACTIVE", "ACTIVE");
    access.history(c.patientId(), "IDENTITY_BOUND");
  }

  public record Grant(
      @NotBlank @Size(max = 200) String proxySubject,
      @NotNull @Pattern(regexp = "RECORDS|APPOINTMENTS|PAYMENTS") String scope,
      @NotNull @Future Instant expiresAt) {
  }

  public UUID grant(UUID patient, Grant c) {
    repository.lock();
    access.requireOwner(patient);
    var old = jdbc.queryForList(
        "SELECT id FROM proxy_grant WHERE patient_id=? AND proxy_subject=? AND scope=?",
        patient,
        c.proxySubject(),
        c.scope());
    UUID id = old.isEmpty() ? UUID.randomUUID() : (UUID) old.getFirst().get("id");
    if (old.isEmpty())
      jdbc.update(
          "INSERT INTO proxy_grant VALUES (?,?,?,?,?,FALSE,?)",
          id,
          patient,
          c.proxySubject(),
          c.scope(),
          c.expiresAt().atOffset(java.time.ZoneOffset.UTC),
          access.actor());
    else
      jdbc.update(
          "UPDATE proxy_grant SET revoked=FALSE,expires_at=?,granted_by=? WHERE id=?",
          c.expiresAt().atOffset(java.time.ZoneOffset.UTC),
          access.actor(),
          id);
    access.history(id, "PROXY_GRANTED");
    return id;
  }

  public void revoke(UUID patient, UUID id) {
    repository.lock();
    access.requireOwner(patient);
    if (jdbc.update("UPDATE proxy_grant SET revoked=TRUE WHERE id=? AND patient_id=?", id, patient) != 1)
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Grant not found");
    access.history(id, "PROXY_REVOKED");
  }

  public List<Map<String, Object>> requests(UUID patient, String type) {
    String table = table(type);
    access.require(patient, type);
    return jdbc.queryForList(
        "SELECT * FROM " + table + " WHERE patient_id=? ORDER BY created_at DESC", patient);
  }

  private String table(String type) {
    return switch (type) {
      case "RECORDS" -> "record_access_request";
      case "APPOINTMENTS" -> "portal_appointment_request";
      case "PAYMENTS" -> "portal_payment";
      default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown request type");
    };
  }

  public void cancelAppointment(UUID id) {
    repository.lock();
    var row = find("portal_appointment_request", id);
    access.require((UUID) row.get("patient_id"), "APPOINTMENTS");
    if (!"REQUESTED".equals(row.get("status")))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Request cannot be cancelled");
    jdbc.update("UPDATE portal_appointment_request SET status='CANCELLED' WHERE id=?", id);
    access.history(id, "APPOINTMENT_CANCELLED");
  }

  public record AppointmentResolution(
      @NotNull @Pattern(regexp = "SCHEDULED|DECLINED") String status,
      @NotBlank String appointmentReference) {
  }

  public void resolveAppointment(UUID id, AppointmentResolution c) {
    repository.lock();
    admin();
    var row = find("portal_appointment_request", id);
    if (!"REQUESTED".equals(row.get("status")))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Request already resolved");
    jdbc.update("UPDATE portal_appointment_request SET status=? WHERE id=?", c.status(), id);
    jdbc.update(
        "INSERT INTO portal_appointment_resolution VALUES (?,?)", id, c.appointmentReference());
    access.history(id, "APPOINTMENT_" + c.status());
  }

  public record Release(@NotBlank @Size(max = 1000000) String content) {
  }

  public void release(UUID id, Release c) {
    repository.lock();
    admin();
    var row = find("record_access_request", id);
    if (!"READY_FOR_REVIEW".equals(row.get("status")))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Request already resolved");
    jdbc.update(
        "INSERT INTO record_release VALUES (?,?,?,CURRENT_TIMESTAMP)",
        id,
        c.content(),
        access.actor());
    jdbc.update("UPDATE record_access_request SET status='FULFILLED' WHERE id=?", id);
    access.history(id, "RECORDS_RELEASED");
  }

  public String download(UUID id) {
    var row = find("record_access_request", id);
    access.require((UUID) row.get("patient_id"), "RECORDS");
    if (!"FULFILLED".equals(row.get("status")))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Records not ready");
    access.history(id, "RECORDS_DOWNLOADED");
    return jdbc.queryForObject(
        "SELECT content FROM record_release WHERE request_id=?", String.class, id);
  }

  private Map<String, Object> find(String table, UUID id) {
    return jdbc.queryForList("SELECT * FROM " + table + " WHERE id=?", id).stream()
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
  }

  private void admin() {
    if (!access.admin())
      throw new org.springframework.security.access.AccessDeniedException("Admin required");
  }

  public record Settlement(@NotBlank @Size(max = 180) String providerReference) {
  }

  public Map<String, Object> settle(UUID id, Settlement c) {
    repository.lock();
    admin();
    var payment = find("portal_payment", id);
    var old = jdbc.queryForList("SELECT * FROM portal_settlement WHERE payment_id=?", id);
    if (!old.isEmpty()) {
      if (!c.providerReference().equals(old.getFirst().get("provider_reference")))
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment already settled");
      return old.getFirst();
    }
    UUID invoiceId = (UUID) payment.get("invoice_id");
    var invoice = downstream.invoice(invoiceId);
    if (!payment.get("patient_id").toString().equals(invoice.get("patient_id").toString()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Invoice patient mismatch");
    var result = downstream.post(
        invoiceId,
        Map.of(
            "reference",
            "portal:" + c.providerReference(),
            "amount",
            payment.get("amount"),
            "kind",
            "PAYMENT"));
    jdbc.update(
        "INSERT INTO portal_settlement VALUES (?,?,?)",
        id,
        c.providerReference(),
        UUID.fromString(result.get("id").toString()));
    jdbc.update("UPDATE portal_payment SET status='SETTLED' WHERE id=?", id);
    access.history(id, "PAYMENT_SETTLED");
    return result;
  }
}
