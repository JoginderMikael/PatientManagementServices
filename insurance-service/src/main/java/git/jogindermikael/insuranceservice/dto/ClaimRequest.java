package git.jogindermikael.insuranceservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ClaimRequest(@NotNull UUID patientId, @NotNull UUID policyId, @NotNull UUID invoiceId,
        @NotNull @Digits(integer = 17, fraction = 2)
        @DecimalMin(value = "0.00", inclusive = false) BigDecimal amount) {
}
