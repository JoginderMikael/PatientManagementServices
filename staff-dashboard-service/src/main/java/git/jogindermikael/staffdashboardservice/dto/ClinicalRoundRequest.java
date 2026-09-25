package git.jogindermikael.staffdashboardservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ClinicalRoundRequest(@NotNull UUID staffId, @NotNull UUID patientId,
        @NotBlank String unit, @NotBlank String notes) {
}
