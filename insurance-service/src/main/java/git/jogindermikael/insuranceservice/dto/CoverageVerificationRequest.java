package git.jogindermikael.insuranceservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CoverageVerificationRequest(@NotNull UUID policyId, @NotBlank String serviceCode,
        @NotNull @Digits(integer = 17, fraction = 2) @DecimalMin("0.00") BigDecimal estimatedCharge) {
}
