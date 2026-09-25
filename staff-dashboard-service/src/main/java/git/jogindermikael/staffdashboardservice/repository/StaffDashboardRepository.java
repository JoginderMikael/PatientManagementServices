package git.jogindermikael.staffdashboardservice.repository;

import git.jogindermikael.staffdashboardservice.model.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StaffDashboardRepository {
  private final JdbcTemplate jdbc;

  public StaffDashboardRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public void lock() {
    jdbc.queryForObject("SELECT id FROM workflow_lock WHERE id=1 FOR UPDATE", Integer.class);
  }

  private ClinicalRound save(ClinicalRound v) {
    if (jdbc.update(
            "UPDATE clinical_round SET staff_id=?, patient_id=?, unit=?, notes=?, status=?,"
                + " updated_at=? WHERE id=?",
            v.staffId(),
            v.patientId(),
            v.unit(),
            v.notes(),
            v.status(),
            v.updatedAt().atOffset(ZoneOffset.UTC),
            v.id())
        == 0)
      jdbc.update(
          "INSERT INTO clinical_round VALUES (?,?,?,?,?,?,?)",
          v.id(),
          v.staffId(),
          v.patientId(),
          v.unit(),
          v.notes(),
          v.status(),
          v.updatedAt().atOffset(ZoneOffset.UTC));
    return v;
  }

  private List<ClinicalRound> rounds(String where, Object... args) {
    return jdbc.query(
        "SELECT * FROM clinical_round " + where + " ORDER BY id",
        (rs, row) ->
            new ClinicalRound(
                rs.getObject("id", UUID.class),
                rs.getObject("staff_id", UUID.class),
                rs.getObject("patient_id", UUID.class),
                rs.getString("unit"),
                rs.getString("notes"),
                rs.getString("status"),
                rs.getObject("updated_at", java.time.OffsetDateTime.class).toInstant()),
        args);
  }

  private PatientChartSummary save(PatientChartSummary v) {
    if (jdbc.update(
            "UPDATE patient_chart_summary SET patient_id=?, summary=?, risk_level=?, updated_at=?"
                + " WHERE id=?",
            v.patientId(),
            v.summary(),
            v.riskLevel(),
            v.updatedAt().atOffset(ZoneOffset.UTC),
            v.id())
        == 0)
      jdbc.update(
          "INSERT INTO patient_chart_summary VALUES (?,?,?,?,?)",
          v.id(),
          v.patientId(),
          v.summary(),
          v.riskLevel(),
          v.updatedAt().atOffset(ZoneOffset.UTC));
    return v;
  }

  private List<PatientChartSummary> charts(String where, Object... args) {
    return jdbc.query(
        "SELECT * FROM patient_chart_summary " + where + " ORDER BY id",
        (rs, row) ->
            new PatientChartSummary(
                rs.getObject("id", UUID.class),
                rs.getObject("patient_id", UUID.class),
                rs.getString("summary"),
                rs.getString("risk_level"),
                rs.getObject("updated_at", java.time.OffsetDateTime.class).toInstant()),
        args);
  }

  private StaffTask save(StaffTask v) {
    if (jdbc.update(
            "UPDATE staff_task SET assignee_id=?, patient_id=?, title=?, priority=?, status=?,"
                + " updated_at=? WHERE id=?",
            v.assigneeId(),
            v.patientId(),
            v.title(),
            v.priority(),
            v.status(),
            v.updatedAt().atOffset(ZoneOffset.UTC),
            v.id())
        == 0)
      jdbc.update(
          "INSERT INTO staff_task VALUES (?,?,?,?,?,?,?)",
          v.id(),
          v.assigneeId(),
          v.patientId(),
          v.title(),
          v.priority(),
          v.status(),
          v.updatedAt().atOffset(ZoneOffset.UTC));
    return v;
  }

  private List<StaffTask> tasks(String where, Object... args) {
    return jdbc.query(
        "SELECT * FROM staff_task " + where + " ORDER BY id",
        (rs, row) ->
            new StaffTask(
                rs.getObject("id", UUID.class),
                rs.getObject("assignee_id", UUID.class),
                rs.getObject("patient_id", UUID.class),
                rs.getString("title"),
                rs.getString("priority"),
                rs.getString("status"),
                rs.getObject("updated_at", java.time.OffsetDateTime.class).toInstant()),
        args);
  }

  public ClinicalRound saveRound(ClinicalRound round) {
    return save(round);
  }

  public Collection<ClinicalRound> findRounds() {
    return rounds("");
  }

  public PatientChartSummary saveChart(PatientChartSummary chart) {
    return save(chart);
  }

  public Collection<PatientChartSummary> findCharts() {
    return charts("");
  }

  public StaffTask saveTask(StaffTask task) {
    return save(task);
  }

  public Optional<StaffTask> findTaskById(UUID id) {
    return tasks("WHERE id=?", id).stream().findFirst();
  }

  public Collection<StaffTask> findTasks() {
    return tasks("");
  }

  public Collection<ClinicalRound> roundsForPatient(UUID id) {
    return rounds("WHERE staff_id=?", id);
  }

  public Collection<PatientChartSummary> chartsForPatient(UUID id) {
    return charts("WHERE patient_id=?", id);
  }

  public Collection<StaffTask> tasksForPatient(UUID id) {
    return tasks("WHERE patient_id=?", id);
  }
}
