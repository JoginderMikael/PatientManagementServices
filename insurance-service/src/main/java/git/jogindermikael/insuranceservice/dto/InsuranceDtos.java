package git.jogindermikael.insuranceservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public final class InsuranceDtos {
  private InsuranceDtos() {}

  public record PolicyRequest(
      @NotNull UUID patientId,
      @NotBlank @jakarta.validation.constraints.Size(max = 200) String providerName,
      @NotBlank @jakarta.validation.constraints.Size(max = 200) String memberNumber,
      @NotBlank @jakarta.validation.constraints.Size(max = 200) String planName) {}

  public record CoverageVerificationRequest(
      @NotNull UUID policyId,
      @NotBlank String serviceCode,
      @NotNull
          @jakarta.validation.constraints.Digits(integer = 17, fraction = 2)
          @DecimalMin("0.00")
          BigDecimal estimatedCharge) {}

  public record ClaimRequest(
      @NotNull UUID patientId,
      @NotNull UUID policyId,
      @NotNull UUID invoiceId,
      @NotNull
          @jakarta.validation.constraints.Digits(integer = 17, fraction = 2)
          @DecimalMin(value = "0.00", inclusive = false)
          BigDecimal amount) {}

  public record ClaimAdjudicationRequest(
      @NotBlank String status,
      @NotNull
          @jakarta.validation.constraints.Digits(integer = 17, fraction = 2)
          @DecimalMin("0.00")
          BigDecimal approvedAmount) {}
}
