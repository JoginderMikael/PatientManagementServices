package git.jogindermikael.insuranceservice.controller;

import git.jogindermikael.insuranceservice.dto.*;
import git.jogindermikael.insuranceservice.model.*;
import git.jogindermikael.insuranceservice.service.InsuranceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasAnyRole('ADMIN','BILLING_STAFF')")
@RequestMapping("/insurance")
@Tag(
    name = "Insurance & Claims",
    description = "Patient insurance policies, coverage verification and insurance claims")
public class InsuranceController {
  private final InsuranceService insuranceService;

  public InsuranceController(InsuranceService insuranceService) {
    this.insuranceService = insuranceService;
  }

  @PostMapping("/policies")
  @Operation(summary = "Register patient insurance details")
  public ResponseEntity<InsurancePolicy> registerPolicy(@Valid @RequestBody PolicyRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(insuranceService.registerPolicy(request));
  }

  @GetMapping("/policies/{patientId}")
  @Operation(summary = "List insurance policies for a patient")
  public List<InsurancePolicy> policiesForPatient(@PathVariable UUID patientId) {
    return insuranceService.policiesForPatient(patientId);
  }

  @PostMapping("/coverage-verifications")
  @Operation(summary = "Verify patient insurance coverage")
  public ResponseEntity<CoverageVerification> verifyCoverage(
      @Valid @RequestBody CoverageVerificationRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(insuranceService.verifyCoverage(request));
  }

  @GetMapping("/coverage-verifications")
  @Operation(summary = "List coverage verifications")
  public List<CoverageVerification> listCoverageVerifications() {
    return insuranceService.listCoverageVerifications();
  }

  @PostMapping("/claims")
  @Operation(summary = "Submit an insurance claim")
  public ResponseEntity<Claim> submitClaim(@Valid @RequestBody ClaimRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(insuranceService.submitClaim(request));
  }

  @PostMapping("/claims/{id}/adjudicate")
  @Operation(summary = "Adjudicate an insurance claim")
  public Claim adjudicateClaim(
      @PathVariable UUID id, @Valid @RequestBody ClaimAdjudicationRequest request) {
    return insuranceService.adjudicateClaim(id, request);
  }

  @GetMapping("/claims")
  @Operation(summary = "List insurance claims")
  public List<Claim> listClaims() {
    return insuranceService.listClaims();
  }

  @PostMapping("/policies/{id}/eligibility-evidence")
  public void evidence(
      @PathVariable UUID id, @Valid @RequestBody EligibilityEvidence request) {
    insuranceService.recordEvidence(id, request);
  }

  @PostMapping("/claims/{id}/remittances")
  public java.util.Map<String, Object> remit(
      @PathVariable UUID id, @Valid @RequestBody RemittanceRequest request) {
    return insuranceService.remit(id, request);
  }

  @PostMapping("/claims/{id}/reconcile")
  public java.util.Map<String, Object> reconcile(@PathVariable UUID id) {
    return insuranceService.reconcile(id);
  }
}
