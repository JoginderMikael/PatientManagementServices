package git.jogindermikael.insuranceservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public final class InsuranceDtos {
    private InsuranceDtos() {
    }

    public record PolicyRequest(@NotNull UUID patientId, @NotBlank String providerName, @NotBlank String memberNumber,
            @NotBlank String planName) {
    }

    public record CoverageVerificationRequest(@NotNull UUID policyId, @NotBlank String serviceCode,
            @NotNull @DecimalMin("0.00") BigDecimal estimatedCharge) {
    }

    public record ClaimRequest(@NotNull UUID patientId, @NotNull UUID policyId, @NotNull UUID invoiceId,
            @NotNull @DecimalMin("0.00") BigDecimal amount) {
    }

    public record ClaimAdjudicationRequest(@NotBlank String status,
            @NotNull @DecimalMin("0.00") BigDecimal approvedAmount) {
    }
}
