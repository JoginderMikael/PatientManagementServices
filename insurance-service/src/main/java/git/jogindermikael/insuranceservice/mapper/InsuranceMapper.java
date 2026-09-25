package git.jogindermikael.insuranceservice.mapper;

import git.jogindermikael.insuranceservice.dto.*;
import git.jogindermikael.insuranceservice.model.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class InsuranceMapper {
  public InsurancePolicy toPolicy(PolicyRequest request) {
    return new InsurancePolicy(
        UUID.randomUUID(),
        request.patientId(),
        request.providerName(),
        request.memberNumber(),
        request.planName(),
        "ACTIVE",
        Instant.now());
  }

  public CoverageVerification toVerification(CoverageVerificationRequest request) {
    BigDecimal insuranceResponsibility = BigDecimal.ZERO;
    BigDecimal patientResponsibility = request.estimatedCharge().subtract(insuranceResponsibility);
    return new CoverageVerification(
        UUID.randomUUID(),
        request.policyId(),
        request.serviceCode(),
        "PENDING",
        insuranceResponsibility,
        patientResponsibility,
        Instant.now());
  }

  public Claim toClaim(ClaimRequest request) {
    return new Claim(
        UUID.randomUUID(),
        request.patientId(),
        request.policyId(),
        request.invoiceId(),
        request.amount(),
        "SUBMITTED",
        Instant.now());
  }

  public Claim toAdjudicatedClaim(Claim claim, ClaimAdjudicationRequest request) {
    return new Claim(
        claim.id(),
        claim.patientId(),
        claim.policyId(),
        claim.invoiceId(),
        claim.amount(),
        request.status(),
        Instant.now());
  }
}
