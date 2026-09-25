package git.jogindermikael.patientportalservice.service;

import git.jogindermikael.patientportalservice.dto.*;
import git.jogindermikael.patientportalservice.mapper.PatientPortalMapper;
import git.jogindermikael.patientportalservice.model.*;
import git.jogindermikael.patientportalservice.repository.PatientPortalRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@org.springframework.transaction.annotation.Transactional
@Service
public class PatientPortalService {
  private final PatientPortalRepository repository;
  private final PatientPortalMapper mapper;
  private final PortalAccess access;
  private final org.springframework.jdbc.core.JdbcTemplate jdbc;

  public PatientPortalService(
      PatientPortalRepository repository,
      PatientPortalMapper mapper,
      PortalAccess access,
      org.springframework.jdbc.core.JdbcTemplate jdbc) {
    this.access = access;
    this.jdbc = jdbc;
    this.repository = repository;
    this.mapper = mapper;
  }

  public PortalOverview overview(UUID patientId) {
    access.requireOwner(patientId);
    return overviewData(patientId);
  }

  public PortalOverview overviewForCurrentUser() {
    return overviewData(access.patientForActor());
  }

  private PortalOverview overviewData(UUID patientId) {
    return new PortalOverview(
        patientId,
        "ACTIVE",
        java.util.List.copyOf(repository.appointmentRequestsForPatient(patientId)),
        java.util.List.copyOf(repository.recordRequestsForPatient(patientId)),
        java.util.List.copyOf(repository.paymentsForPatient(patientId)));
  }

  public PortalAppointmentRequest requestAppointment(PortalAppointmentCommand request) {
    repository.lock();
    access.require(request.patientId(), "APPOINTMENTS");
    var saved = repository.saveAppointmentRequest(mapper.toAppointmentRequest(request));
    access.history(saved.id(), "APPOINTMENT_REQUESTED");
    return saved;
  }

  public RecordAccessRequest requestRecords(RecordAccessCommand request) {
    repository.lock();
    access.require(request.patientId(), "RECORDS");
    var saved = repository.saveRecordRequest(mapper.toRecordAccessRequest(request));
    access.history(saved.id(), "RECORDS_REQUESTED");
    return saved;
  }

  public PortalPayment payBill(PortalPaymentCommand request, String reference) {
    repository.lock();
    access.require(request.patientId(), "PAYMENTS");
    if (reference == null || reference.isBlank() || reference.length() > 200)
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "Idempotency-Key required (max 200)");
    var existing =
        jdbc.queryForList(
            "SELECT payment_id FROM portal_payment_reference WHERE reference=?", reference);
    if (!existing.isEmpty()) {
      var payment =
          repository.findPayments().stream()
              .filter(v -> v.id().equals(existing.getFirst().get("payment_id")))
              .findFirst()
              .orElseThrow();
      if (!payment.patientId().equals(request.patientId())
          || !payment.invoiceId().equals(request.invoiceId())
          || payment.amount().compareTo(request.amount()) != 0)
        throw new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.CONFLICT, "Payment key reused");
      return payment;
    }
    var saved = repository.savePayment(mapper.toPayment(request));
    jdbc.update("INSERT INTO portal_payment_reference VALUES (?,?)", reference, saved.id());
    access.history(saved.id(), "PAYMENT_SUBMITTED");
    return saved;
  }
}
