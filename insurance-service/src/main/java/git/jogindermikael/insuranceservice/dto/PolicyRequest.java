package git.jogindermikael.insuranceservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record PolicyRequest(@NotNull UUID patientId, @NotBlank @Size(max = 200) String providerName,
        @NotBlank @Size(max = 200) String memberNumber,
        @NotBlank @Size(max = 200) String planName) {
}
