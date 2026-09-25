package git.jogindermikael.inventorypharmacyservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record MedicationRequest(UUID id, @NotBlank String name, @NotBlank String ndcCode,
        @Min(0) int quantityOnHand,
        @NotNull @Digits(integer = 17, fraction = 2) @DecimalMin("0.00") BigDecimal unitCost) {
}
