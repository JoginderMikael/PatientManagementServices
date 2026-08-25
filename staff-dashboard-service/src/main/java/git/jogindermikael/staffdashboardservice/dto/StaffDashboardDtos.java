package git.jogindermikael.staffdashboardservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public final class StaffDashboardDtos {
    private StaffDashboardDtos() {
    }

    public record ClinicalRoundRequest(@NotNull UUID staffId, @NotNull UUID patientId, @NotBlank String unit, @NotBlank String notes) {}
    public record PatientChartRequest(UUID id, @NotNull UUID patientId, @NotBlank String summary, @NotBlank String riskLevel) {}
    public record StaffTaskRequest(@NotNull UUID assigneeId, @NotNull UUID patientId, @NotBlank String title, @NotBlank String priority) {}
}
