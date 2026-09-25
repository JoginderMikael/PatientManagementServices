package git.jogindermikael.staffdashboardservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ClinicalTask(@NotBlank String reference, @NotNull UUID patientId,
        @NotNull UUID assigneeId, @NotBlank @Size(max = 2000) String title) {
}
