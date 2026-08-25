package git.jogindermikael.insuranceservice.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class InsuranceModels {
    private InsuranceModels() {
    }

    public record InsurancePolicy(UUID id, UUID patientId, String providerName, String memberNumber, String planName, String status, Instant createdAt) {}
    public record CoverageVerification(UUID id, UUID policyId, String serviceCode, String status, BigDecimal insuranceResponsibility, BigDecimal patientResponsibility, Instant verifiedAt) {}
    public record Claim(UUID id, UUID patientId, UUID policyId, UUID invoiceId, BigDecimal amount, String status, Instant updatedAt) {}
}
