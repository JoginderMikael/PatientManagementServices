package git.jogindermikael.insuranceservice.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EligibilityEvidence(@NotBlank String serviceCode,
        @FutureOrPresent @NotNull LocalDate validUntil, @NotBlank String payerReference,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal insurancePercent) {
}
