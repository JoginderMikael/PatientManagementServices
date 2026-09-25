package git.jogindermikael.staffdashboardservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PatientChartRequest(UUID id, @NotNull UUID patientId, @NotBlank String summary,
        @NotBlank String riskLevel) {
}
