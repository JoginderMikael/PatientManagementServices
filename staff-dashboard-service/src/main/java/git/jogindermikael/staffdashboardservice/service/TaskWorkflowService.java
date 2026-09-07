package git.jogindermikael.staffdashboardservice.service;

import git.jogindermikael.staffdashboardservice.repository.StaffDashboardRepository;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class TaskWorkflowService {
  private final JdbcTemplate jdbc;
  private final StaffAccess access;
  private final StaffDashboardRepository repository;

  public TaskWorkflowService(
      JdbcTemplate jdbc, StaffAccess access, StaffDashboardRepository repository) {
    this.jdbc = jdbc;
    this.access = access;
    this.repository = repository;
  }

  public void initialize(UUID id, String priority) {
    if (!Set.of("ROUTINE", "HIGH", "URGENT").contains(priority))
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Priority must be ROUTINE, HIGH or URGENT");
    Instant due =
        Instant.now()
            .plusSeconds(
                switch (priority) {
                  case "URGENT" -> 900;
                  case "HIGH" -> 3600;
                  default -> 86400;
                });
    String role = access.role();
    String escalation =
        Set.of("CLINICIAN", "NURSE", "ADMIN").contains(role) ? "CLINICIAN" : "ADMIN";
    jdbc.update(
        "INSERT INTO task_workflow VALUES (?,?,?,?,FALSE)",
        id,
        role,
        due.atOffset(ZoneOffset.UTC),
        escalation);
    history(id, "CREATED", "");
  }

  public List<Map<String, Object>> queue(String role, int limit) {
    access.queue(role);
    return jdbc.queryForList(
        "SELECT t.*,w.due_at,w.escalated,w.queue_role FROM staff_task t JOIN task_workflow w ON"
            + " w.task_id=t.id WHERE w.queue_role=? AND t.status<>'COMPLETED' ORDER BY"
            + " w.due_at,t.id LIMIT ?",
        role,
        Math.max(1, Math.min(200, limit)));
  }

  public void authorize(UUID id) {
    var task =
        repository
            .findTaskById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    access.assignee(task.assigneeId());
    String role =
        jdbc.queryForObject(
            "SELECT queue_role FROM task_workflow WHERE task_id=?", String.class, id);
    access.queue(role);
  }

  public record Handoff(
      @NotNull UUID assigneeId,
      @NotBlank String queueRole,
      @NotNull Instant dueAt,
      @NotBlank String reason) {}

  public void handoff(UUID id, Handoff c) {
    repository.lock();
    authorize(id);
    if (!StaffAccess.ROLES.contains(c.queueRole()))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown queue");
    if (repository.findTaskById(id).orElseThrow().status().equals("COMPLETED"))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Completed task");
    jdbc.update(
        "UPDATE staff_task SET assignee_id=?,updated_at=CURRENT_TIMESTAMP WHERE id=?",
        c.assigneeId(),
        id);
    jdbc.update(
        "UPDATE task_workflow SET queue_role=?,due_at=?,escalated=FALSE WHERE task_id=?",
        c.queueRole(),
        c.dueAt().atOffset(ZoneOffset.UTC),
        id);
    history(id, "HANDOFF", c.reason());
  }

  public record Comment(@NotBlank @Size(max = 2000) String text) {}

  public void comment(UUID id, Comment c) {
    repository.lock();
    authorize(id);
    history(id, "COMMENT", c.text());
  }

  public List<Map<String, Object>> history(UUID id) {
    authorize(id);
    return jdbc.queryForList(
        "SELECT * FROM task_history WHERE task_id=? ORDER BY occurred_at,id", id);
  }

  public void history(UUID id, String action, String comment) {
    jdbc.update(
        "INSERT INTO task_history VALUES (?,?,?,?,?,CURRENT_TIMESTAMP)",
        UUID.randomUUID(),
        id,
        access.actor(),
        action,
        comment);
  }

  public int escalate() {
    repository.lock();
    var due =
        jdbc.queryForList(
            "SELECT w.task_id FROM task_workflow w JOIN staff_task t ON t.id=w.task_id WHERE"
                + " w.escalated=FALSE AND w.due_at<CURRENT_TIMESTAMP AND t.status<>'COMPLETED'");
    for (var row : due) {
      UUID id = (UUID) row.get("task_id");
      jdbc.update(
          "UPDATE task_workflow SET escalated=TRUE,queue_role=escalation_role WHERE task_id=?", id);
      jdbc.update(
          "UPDATE staff_task SET status='ESCALATED',updated_at=CURRENT_TIMESTAMP WHERE id=?", id);
      jdbc.update(
          "INSERT INTO task_history VALUES (?,?,'SYSTEM','ESCALATED','Due time"
              + " exceeded',CURRENT_TIMESTAMP)",
          UUID.randomUUID(),
          id);
    }
    return due.size();
  }

  public void claim(UUID id) {
    repository.lock();
    var row =
        jdbc.queryForList("SELECT queue_role FROM task_workflow WHERE task_id=?", id).stream()
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    access.queue((String) row.get("queue_role"));
    UUID actor;
    try {
      actor = UUID.fromString(access.actor());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff subject must be a UUID");
    }
    if (jdbc.update(
            "UPDATE staff_task SET assignee_id=?,status='IN_PROGRESS',updated_at=CURRENT_TIMESTAMP"
                + " WHERE id=? AND status IN ('OPEN','ESCALATED')",
            actor,
            id)
        != 1)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Task already claimed or completed");
    history(id, "CLAIMED", "");
  }

  public record ClinicalTask(
      @NotBlank String reference,
      @NotNull UUID patientId,
      @NotNull UUID assigneeId,
      @NotBlank @Size(max = 2000) String title) {}

  public Map<String, Object> clinical(ClinicalTask c) {
    repository.lock();
    var old =
        jdbc.queryForList(
            "SELECT t.* FROM staff_task t JOIN clinical_task_source s ON s.task_id=t.id WHERE"
                + " s.reference=?",
            c.reference());
    if (!old.isEmpty()) {
      var row = old.getFirst();
      if (!c.patientId().equals(row.get("patient_id")) || !c.title().equals(row.get("title")))
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Task reference reused");
      return row;
    }
    UUID id = UUID.randomUUID();
    jdbc.update(
        "INSERT INTO staff_task VALUES (?,?,?,?,?,'OPEN',CURRENT_TIMESTAMP)",
        id,
        c.assigneeId(),
        c.patientId(),
        c.title(),
        "URGENT");
    jdbc.update(
        "INSERT INTO task_workflow VALUES (?, 'CLINICIAN', ?, 'CLINICIAN', FALSE)",
        id,
        Instant.now().plusSeconds(900).atOffset(ZoneOffset.UTC));
    jdbc.update("INSERT INTO clinical_task_source VALUES (?,?)", c.reference(), id);
    history(id, "CLINICAL_ALERT", "");
    return jdbc.queryForMap("SELECT * FROM staff_task WHERE id=?", id);
  }
}
