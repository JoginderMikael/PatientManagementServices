package git.jogindermikael.insuranceservice.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CoverageVerification(UUID id, UUID policyId, String serviceCode, String status,
        BigDecimal insuranceResponsibility, BigDecimal patientResponsibility, Instant verifiedAt) {
}
