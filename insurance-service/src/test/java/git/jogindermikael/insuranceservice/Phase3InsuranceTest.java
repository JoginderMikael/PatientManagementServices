package git.jogindermikael.insuranceservice;

import static org.junit.jupiter.api.Assertions.*;

import git.jogindermikael.insuranceservice.dto.InsuranceDtos.*;
import git.jogindermikael.insuranceservice.service.InsuranceService;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(properties = {"app.audit.enabled=false", "grpc.server.port=-1"})
class Phase3InsuranceTest {

  @Autowired InsuranceService service;

  @Autowired StubInvoices downstream;

  @org.springframework.boot.test.context.TestConfiguration
  static class InvoiceFixture {
    @org.springframework.context.annotation.Bean
    @org.springframework.context.annotation.Primary
    StubInvoices invoices() {
      return new StubInvoices();
    }
  }

  static class StubInvoices extends git.jogindermikael.insuranceservice.integration.WorkflowClient {
    Map<String, Object> response = Map.of();

    StubInvoices() {
      super("http://localhost:1", "http://localhost:1");
    }

    @Override
    public Map<String, Object> invoice(UUID id) {
      return response;
    }
  }

  @Test
  void eligibilityRequiresPayerEvidenceAndUnknownPolicyFails() {
    UUID patient = UUID.randomUUID();
    var policy =
        service.registerPolicy(
            new PolicyRequest(patient, "Synthetic payer", UUID.randomUUID().toString(), "Plan"));
    var command = new CoverageVerificationRequest(policy.id(), "TEST", new BigDecimal("100.00"));
    assertEquals("PENDING", service.verifyCoverage(command).status());
    service.recordEvidence(
        policy.id(),
        new InsuranceService.EligibilityEvidence(
            "TEST", LocalDate.now().plusDays(1), "payer-reference", new BigDecimal("65")));
    assertEquals(
        new BigDecimal("65.00"), service.verifyCoverage(command).insuranceResponsibility());
    assertThrows(
        ResponseStatusException.class,
        () ->
            service.verifyCoverage(
                new CoverageVerificationRequest(UUID.randomUUID(), "TEST", BigDecimal.TEN)));
  }

  @Test
  void claimPreservesSubmittedAmountAndRemittanceIsIdempotent() {
    UUID patient = UUID.randomUUID();
    var policy =
        service.registerPolicy(
            new PolicyRequest(patient, "Payer", UUID.randomUUID().toString(), "Plan"));
    var command =
        new ClaimRequest(patient, policy.id(), UUID.randomUUID(), new BigDecimal("100.00"));
    downstream.response =
        Map.of("patient_id", patient.toString(), "amount", new BigDecimal("100.00"));
    var claim = service.submitClaim(command);
    assertEquals(claim.id(), service.submitClaim(command).id());
    assertThrows(
        ResponseStatusException.class,
        () ->
            service.submitClaim(
                new ClaimRequest(
                    UUID.randomUUID(), policy.id(), UUID.randomUUID(), BigDecimal.TEN)));
    assertThrows(
        ResponseStatusException.class,
        () ->
            service.adjudicateClaim(
                claim.id(), new ClaimAdjudicationRequest("APPROVED", new BigDecimal("101.00"))));
    var approved =
        service.adjudicateClaim(
            claim.id(), new ClaimAdjudicationRequest("APPROVED", new BigDecimal("70.00")));
    assertEquals(new BigDecimal("100.00"), approved.amount());
    var remittance =
        new InsuranceService.RemittanceRequest(
            UUID.randomUUID().toString(), new BigDecimal("70.00"));
    var posted = service.remit(claim.id(), remittance);
    assertEquals(posted.get("id"), service.remit(claim.id(), remittance).get("id"));
    assertThrows(
        ResponseStatusException.class,
        () ->
            service.remit(
                claim.id(),
                new InsuranceService.RemittanceRequest(remittance.reference(), BigDecimal.ONE)));
  }
}
