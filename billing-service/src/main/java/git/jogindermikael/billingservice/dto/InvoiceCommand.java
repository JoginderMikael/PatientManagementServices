package git.jogindermikael.billingservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceCommand(@NotNull UUID patientId, @NotBlank String reference,
        @NotNull @DecimalMin(value = "0", inclusive = false)
        @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @Pattern(regexp = "[A-Z]{3}") @NotNull String currency) {
}
