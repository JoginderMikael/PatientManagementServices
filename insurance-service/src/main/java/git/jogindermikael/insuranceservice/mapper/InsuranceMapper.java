package git.jogindermikael.insuranceservice.mapper;

import git.jogindermikael.insuranceservice.dto.InsuranceDtos.*;
import git.jogindermikael.insuranceservice.model.InsuranceModels.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class InsuranceMapper {
    public InsurancePolicy toPolicy(PolicyRequest request) {
        return new InsurancePolicy(UUID.randomUUID(), request.patientId(), request.providerName(), request.memberNumber(), request.planName(), "ACTIVE", Instant.now());
    }

    public CoverageVerification toVerification(CoverageVerificationRequest request) {
        BigDecimal insuranceResponsibility = request.estimatedCharge().multiply(new BigDecimal("0.80"));
        BigDecimal patientResponsibility = request.estimatedCharge().subtract(insuranceResponsibility);
        return new CoverageVerification(UUID.randomUUID(), request.policyId(), request.serviceCode(), "VERIFIED", insuranceResponsibility, patientResponsibility, Instant.now());
    }

    public Claim toClaim(ClaimRequest request) {
        return new Claim(UUID.randomUUID(), request.patientId(), request.policyId(), request.invoiceId(), request.amount(), "SUBMITTED", Instant.now());
    }

    public Claim toAdjudicatedClaim(Claim claim, ClaimAdjudicationRequest request) {
        return new Claim(claim.id(), claim.patientId(), claim.policyId(), claim.invoiceId(), request.approvedAmount(), request.status(), Instant.now());
    }
}
