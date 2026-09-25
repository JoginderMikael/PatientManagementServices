package git.jogindermikael.patientportalservice;

import static org.junit.jupiter.api.Assertions.*;

import git.jogindermikael.patientportalservice.dto.*;
import git.jogindermikael.patientportalservice.service.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(properties = {"app.audit.enabled=false", "grpc.server.port=-1"})
class Phase3PortalTest {

  @Autowired PatientPortalService service;
  @Autowired PortalWorkflowService workflow;
  @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
  @Autowired PatientLifecycleConsumer lifecycle;

  private void identity(String actor, String role) {
    org.springframework.security.core.context.SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                actor,
                "",
                List.of(
                    new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_" + role))));
  }

  @AfterEach
  void clear() {
    org.springframework.security.core.context.SecurityContextHolder.clearContext();
  }

  @Test
  void patientOwnershipAndProxyRevocationProtectRecords() {
    UUID patient = UUID.randomUUID();
    String owner = UUID.randomUUID().toString(), proxy = UUID.randomUUID().toString();
    identity("admin", "ADMIN");
    workflow.bind(new Identity(owner, patient));
    identity(owner, "PATIENT");
    UUID grant =
        workflow.grant(
            patient,
            new Grant(proxy, "RECORDS", Instant.now().plusSeconds(3600)));
    identity(proxy, "PATIENT");
    var request = service.requestRecords(new RecordAccessCommand(patient, "SUMMARY"));
    assertThrows(
        org.springframework.security.access.AccessDeniedException.class,
        () ->
            service.requestAppointment(new PortalAppointmentCommand(patient, "General", "Visit")));
    identity("admin", "ADMIN");
    workflow.release(request.id(), new Release("Synthetic released record"));
    identity(proxy, "PATIENT");
    assertEquals("Synthetic released record", workflow.download(request.id()));
    identity(owner, "PATIENT");
    workflow.revoke(patient, grant);
    identity(proxy, "PATIENT");
    assertThrows(
        org.springframework.security.access.AccessDeniedException.class,
        () -> workflow.download(request.id()));
  }

  @Test
  void foreignPatientAndExpiredProxyAreDenied() {
    UUID patient = UUID.randomUUID();
    String subject = UUID.randomUUID().toString();
    identity(subject, "PATIENT");
    assertThrows(
        org.springframework.security.access.AccessDeniedException.class,
        () -> service.overview(patient));
    identity("admin", "ADMIN");
    UUID grant =
        workflow.grant(
            patient,
            new Grant(subject, "RECORDS", Instant.now().plusSeconds(60)));
    jdbc.update(
        "UPDATE proxy_grant SET expires_at=? WHERE id=?",
        Instant.now().minusSeconds(1).atOffset(ZoneOffset.UTC),
        grant);
    identity(subject, "PATIENT");
    assertThrows(
        org.springframework.security.access.AccessDeniedException.class,
        () -> service.requestRecords(new RecordAccessCommand(patient, "SUMMARY")));
  }

  @Test
  void currentPatientIsResolvedServerSideAndSuspendedOnArchive() throws Exception {
    UUID patient = UUID.randomUUID();
    String subject = UUID.randomUUID().toString();
    identity("admin", "ADMIN");
    workflow.bind(new Identity(subject, patient));
    identity(subject, "PATIENT");
    assertEquals(patient, service.overviewForCurrentUser().patientId());

    lifecycle.consume(("""
        {"schemaVersion":1,"eventType":"PATIENT_ARCHIVED","patientId":"%s",\
         "payload":{"status":"ARCHIVED"}}
        """).formatted(patient).getBytes(java.nio.charset.StandardCharsets.UTF_8));

    assertThrows(
        ResponseStatusException.class,
        service::overviewForCurrentUser);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void appointmentCancellationAndPaymentRetries() {
    UUID patient = UUID.randomUUID();
    var appt =
        service.requestAppointment(new PortalAppointmentCommand(patient, "General", "Visit"));
    workflow.cancelAppointment(appt.id());
    assertThrows(
        ResponseStatusException.class,
        () ->
            workflow.resolveAppointment(
                appt.id(), new AppointmentResolution("SCHEDULED", "ref")));
    var command = new PortalPaymentCommand(patient, UUID.randomUUID(), BigDecimal.TEN);
    String key = UUID.randomUUID().toString();
    assertEquals(service.payBill(command, key).id(), service.payBill(command, key).id());
    assertThrows(
        ResponseStatusException.class,
        () ->
            service.payBill(
                new PortalPaymentCommand(patient, command.invoiceId(), BigDecimal.ONE), key));
  }
}
