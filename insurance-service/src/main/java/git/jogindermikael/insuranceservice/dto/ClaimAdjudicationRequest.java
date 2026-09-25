package git.jogindermikael.insuranceservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ClaimAdjudicationRequest(@NotBlank String status,
        @NotNull @Digits(integer = 17, fraction = 2) @DecimalMin("0.00") BigDecimal approvedAmount) {
}
