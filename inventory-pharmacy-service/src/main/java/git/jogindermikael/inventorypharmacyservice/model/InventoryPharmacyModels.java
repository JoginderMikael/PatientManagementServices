package git.jogindermikael.inventorypharmacyservice.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class InventoryPharmacyModels {
    private InventoryPharmacyModels() {
    }

    public record SupplyItem(UUID id, String name, int quantityOnHand, int reorderThreshold, BigDecimal unitCost,
            Instant updatedAt) {
    }

    public record MedicationStock(UUID id, String name, String ndcCode, int quantityOnHand, BigDecimal unitCost,
            Instant updatedAt) {
    }

    public record PharmacyPrescription(UUID id, UUID ehrPrescriptionId, UUID patientId, String medication, int quantity,
            String status, Instant updatedAt) {
    }

    public record MedicationCharge(UUID id, UUID patientId, UUID prescriptionId, BigDecimal amount, String status,
            Instant createdAt) {
    }
}
