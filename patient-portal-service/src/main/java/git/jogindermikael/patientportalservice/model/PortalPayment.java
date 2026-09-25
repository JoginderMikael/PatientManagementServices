package git.jogindermikael.patientportalservice.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PortalPayment(UUID id, UUID patientId, UUID invoiceId, BigDecimal amount,
        String status, Instant createdAt) {
}
