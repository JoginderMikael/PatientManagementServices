package git.jogindermikael.inventorypharmacyservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record MedicationChargeRequest(@NotNull UUID patientId, @NotNull UUID prescriptionId,
        @NotNull @Digits(integer = 17, fraction = 2) @DecimalMin("0.00") BigDecimal amount) {
}
