package git.jogindermikael.billingservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record PaymentCommand(@NotBlank String reference,
        @NotNull @DecimalMin(value = "0", inclusive = false)
        @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @Pattern(regexp = "PAYMENT|REMITTANCE") @NotNull String kind) {
}
