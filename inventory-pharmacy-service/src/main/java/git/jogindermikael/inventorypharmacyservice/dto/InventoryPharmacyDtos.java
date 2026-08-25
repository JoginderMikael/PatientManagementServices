package git.jogindermikael.inventorypharmacyservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public final class InventoryPharmacyDtos {
    private InventoryPharmacyDtos() {
    }

    public record SupplyRequest(UUID id, @NotBlank String name, @Min(0) int quantityOnHand, @Min(0) int reorderThreshold, @NotNull @DecimalMin("0.00") BigDecimal unitCost) {}
    public record MedicationRequest(UUID id, @NotBlank String name, @NotBlank String ndcCode, @Min(0) int quantityOnHand, @NotNull @DecimalMin("0.00") BigDecimal unitCost) {}
    public record PharmacyPrescriptionRequest(@NotNull UUID ehrPrescriptionId, @NotNull UUID patientId, @NotBlank String medication, @Min(1) int quantity) {}
    public record MedicationChargeRequest(@NotNull UUID patientId, @NotNull UUID prescriptionId, @NotNull @DecimalMin("0.00") BigDecimal amount) {}
}
