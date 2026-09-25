package git.jogindermikael.insuranceservice.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Claim(UUID id, UUID patientId, UUID policyId, UUID invoiceId, BigDecimal amount,
        String status, Instant updatedAt) {
}
