package git.jogindermikael.staffdashboardservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record StaffTaskRequest(@NotNull UUID assigneeId, @NotNull UUID patientId,
        @NotBlank String title, @NotBlank String priority) {
}
