package git.jogindermikael.inventorypharmacyservice.model;

import java.time.Instant;
import java.util.UUID;

public record PharmacyPrescription(UUID id, UUID ehrPrescriptionId, UUID patientId,
        String medication, int quantity, String status, Instant updatedAt) {
}
