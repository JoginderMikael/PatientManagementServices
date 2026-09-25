package git.jogindermikael.insuranceservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RemittanceRequest(@NotBlank String reference,
        @NotNull @DecimalMin(value = "0", inclusive = false)
        @Digits(integer = 17, fraction = 2) BigDecimal paidAmount) {
}
