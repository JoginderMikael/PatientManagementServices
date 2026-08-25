package git.jogindermikael.insuranceservice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class InsuranceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InsuranceServiceApplication.class, args);
    }
}

@RestController
@RequestMapping("/insurance")
@Tag(name = "Insurance & Claims", description = "Patient insurance policies, coverage verification and insurance claims")
class InsuranceController {
    private final Map<UUID, InsurancePolicy> policies = new ConcurrentHashMap<>();
    private final Map<UUID, CoverageVerification> verifications = new ConcurrentHashMap<>();
    private final Map<UUID, Claim> claims = new ConcurrentHashMap<>();

    @PostMapping("/policies")
    @Operation(summary = "Register patient insurance details")
    ResponseEntity<InsurancePolicy> registerPolicy(@Valid @RequestBody PolicyRequest request) {
        UUID id = UUID.randomUUID();
        InsurancePolicy policy = new InsurancePolicy(id, request.patientId(), request.providerName(), request.memberNumber(), request.planName(), "ACTIVE", Instant.now());
        policies.put(id, policy);
        return ResponseEntity.status(HttpStatus.CREATED).body(policy);
    }

    @GetMapping("/policies/{patientId}")
    @Operation(summary = "List insurance policies for a patient")
    List<InsurancePolicy> policiesForPatient(@PathVariable UUID patientId) {
        return policies.values().stream().filter(policy -> policy.patientId().equals(patientId)).toList();
    }

    @PostMapping("/coverage-verifications")
    @Operation(summary = "Verify patient insurance coverage")
    ResponseEntity<CoverageVerification> verifyCoverage(@Valid @RequestBody CoverageVerificationRequest request) {
        UUID id = UUID.randomUUID();
        BigDecimal insuranceResponsibility = request.estimatedCharge().multiply(new BigDecimal("0.80"));
        BigDecimal patientResponsibility = request.estimatedCharge().subtract(insuranceResponsibility);
        CoverageVerification verification = new CoverageVerification(id, request.policyId(), request.serviceCode(), "VERIFIED", insuranceResponsibility, patientResponsibility, Instant.now());
        verifications.put(id, verification);
        return ResponseEntity.status(HttpStatus.CREATED).body(verification);
    }

    @GetMapping("/coverage-verifications")
    @Operation(summary = "List coverage verifications")
    List<CoverageVerification> listCoverageVerifications() {
        return verifications.values().stream().sorted(Comparator.comparing(CoverageVerification::verifiedAt)).toList();
    }

    @PostMapping("/claims")
    @Operation(summary = "Submit an insurance claim")
    ResponseEntity<Claim> submitClaim(@Valid @RequestBody ClaimRequest request) {
        UUID id = UUID.randomUUID();
        Claim claim = new Claim(id, request.patientId(), request.policyId(), request.invoiceId(), request.amount(), "SUBMITTED", Instant.now());
        claims.put(id, claim);
        return ResponseEntity.status(HttpStatus.CREATED).body(claim);
    }

    @PostMapping("/claims/{id}/adjudicate")
    @Operation(summary = "Adjudicate an insurance claim")
    Claim adjudicateClaim(@PathVariable UUID id, @Valid @RequestBody ClaimAdjudicationRequest request) {
        Claim claim = Optional.ofNullable(claims.get(id)).orElseThrow();
        Claim adjudicated = new Claim(claim.id(), claim.patientId(), claim.policyId(), claim.invoiceId(), request.approvedAmount(), request.status(), Instant.now());
        claims.put(id, adjudicated);
        return adjudicated;
    }

    @GetMapping("/claims")
    @Operation(summary = "List insurance claims")
    List<Claim> listClaims() {
        return claims.values().stream().sorted(Comparator.comparing(Claim::updatedAt)).toList();
    }
}

record InsurancePolicy(UUID id, UUID patientId, String providerName, String memberNumber, String planName, String status, Instant createdAt) {}
record CoverageVerification(UUID id, UUID policyId, String serviceCode, String status, BigDecimal insuranceResponsibility, BigDecimal patientResponsibility, Instant verifiedAt) {}
record Claim(UUID id, UUID patientId, UUID policyId, UUID invoiceId, BigDecimal amount, String status, Instant updatedAt) {}

record PolicyRequest(@NotNull UUID patientId, @NotBlank String providerName, @NotBlank String memberNumber, @NotBlank String planName) {}
record CoverageVerificationRequest(@NotNull UUID policyId, @NotBlank String serviceCode, @NotNull @DecimalMin("0.00") BigDecimal estimatedCharge) {}
record ClaimRequest(@NotNull UUID patientId, @NotNull UUID policyId, @NotNull UUID invoiceId, @NotNull @DecimalMin("0.00") BigDecimal amount) {}
record ClaimAdjudicationRequest(@NotBlank String status, @NotNull @DecimalMin("0.00") BigDecimal approvedAmount) {}
