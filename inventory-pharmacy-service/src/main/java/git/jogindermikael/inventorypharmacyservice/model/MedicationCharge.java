package git.jogindermikael.inventorypharmacyservice.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MedicationCharge(UUID id, UUID patientId, UUID prescriptionId, BigDecimal amount,
        String status, Instant createdAt) {
}
