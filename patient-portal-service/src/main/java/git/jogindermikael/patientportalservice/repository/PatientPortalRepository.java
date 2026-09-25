package git.jogindermikael.patientportalservice.repository;

import git.jogindermikael.patientportalservice.model.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PatientPortalRepository {
  private final JdbcTemplate jdbc;

  public PatientPortalRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public void lock() {
    jdbc.queryForObject("SELECT id FROM workflow_lock WHERE id=1 FOR UPDATE", Integer.class);
  }

  private PortalAppointmentRequest save(PortalAppointmentRequest v) {
    if (jdbc.update(
            "UPDATE portal_appointment_request SET patient_id=?, preferred_specialty=?, reason=?,"
                + " status=?, created_at=? WHERE id=?",
            v.patientId(),
            v.preferredSpecialty(),
            v.reason(),
            v.status(),
            v.createdAt().atOffset(ZoneOffset.UTC),
            v.id())
        == 0)
      jdbc.update(
          "INSERT INTO portal_appointment_request VALUES (?,?,?,?,?,?)",
          v.id(),
          v.patientId(),
          v.preferredSpecialty(),
          v.reason(),
          v.status(),
          v.createdAt().atOffset(ZoneOffset.UTC));
    return v;
  }

  private List<PortalAppointmentRequest> appointmentRequests(String where, Object... args) {
    return jdbc.query(
        "SELECT * FROM portal_appointment_request " + where + " ORDER BY id",
        (rs, row) ->
            new PortalAppointmentRequest(
                rs.getObject("id", UUID.class),
                rs.getObject("patient_id", UUID.class),
                rs.getString("preferred_specialty"),
                rs.getString("reason"),
                rs.getString("status"),
                rs.getObject("created_at", java.time.OffsetDateTime.class).toInstant()),
        args);
  }

  private RecordAccessRequest save(RecordAccessRequest v) {
    if (jdbc.update(
            "UPDATE record_access_request SET patient_id=?, record_type=?, status=?, created_at=?"
                + " WHERE id=?",
            v.patientId(),
            v.recordType(),
            v.status(),
            v.createdAt().atOffset(ZoneOffset.UTC),
            v.id())
        == 0)
      jdbc.update(
          "INSERT INTO record_access_request VALUES (?,?,?,?,?)",
          v.id(),
          v.patientId(),
          v.recordType(),
          v.status(),
          v.createdAt().atOffset(ZoneOffset.UTC));
    return v;
  }

  private List<RecordAccessRequest> recordRequests(String where, Object... args) {
    return jdbc.query(
        "SELECT * FROM record_access_request " + where + " ORDER BY id",
        (rs, row) ->
            new RecordAccessRequest(
                rs.getObject("id", UUID.class),
                rs.getObject("patient_id", UUID.class),
                rs.getString("record_type"),
                rs.getString("status"),
                rs.getObject("created_at", java.time.OffsetDateTime.class).toInstant()),
        args);
  }

  private PortalPayment save(PortalPayment v) {
    if (jdbc.update(
            "UPDATE portal_payment SET patient_id=?, invoice_id=?, amount=?, status=?, created_at=?"
                + " WHERE id=?",
            v.patientId(),
            v.invoiceId(),
            v.amount(),
            v.status(),
            v.createdAt().atOffset(ZoneOffset.UTC),
            v.id())
        == 0)
      jdbc.update(
          "INSERT INTO portal_payment VALUES (?,?,?,?,?,?)",
          v.id(),
          v.patientId(),
          v.invoiceId(),
          v.amount(),
          v.status(),
          v.createdAt().atOffset(ZoneOffset.UTC));
    return v;
  }

  private List<PortalPayment> payments(String where, Object... args) {
    return jdbc.query(
        "SELECT * FROM portal_payment " + where + " ORDER BY id",
        (rs, row) ->
            new PortalPayment(
                rs.getObject("id", UUID.class),
                rs.getObject("patient_id", UUID.class),
                rs.getObject("invoice_id", UUID.class),
                rs.getBigDecimal("amount"),
                rs.getString("status"),
                rs.getObject("created_at", java.time.OffsetDateTime.class).toInstant()),
        args);
  }

  public PortalAppointmentRequest saveAppointmentRequest(PortalAppointmentRequest request) {
    return save(request);
  }

  public Collection<PortalAppointmentRequest> findAppointmentRequests() {
    return appointmentRequests("");
  }

  public RecordAccessRequest saveRecordRequest(RecordAccessRequest request) {
    return save(request);
  }

  public Collection<RecordAccessRequest> findRecordRequests() {
    return recordRequests("");
  }

  public PortalPayment savePayment(PortalPayment payment) {
    return save(payment);
  }

  public Collection<PortalPayment> findPayments() {
    return payments("");
  }

  public Collection<PortalAppointmentRequest> appointmentRequestsForPatient(UUID id) {
    return appointmentRequests("WHERE patient_id=?", id);
  }

  public Collection<RecordAccessRequest> recordRequestsForPatient(UUID id) {
    return recordRequests("WHERE patient_id=?", id);
  }

  public Collection<PortalPayment> paymentsForPatient(UUID id) {
    return payments("WHERE patient_id=?", id);
  }
}
