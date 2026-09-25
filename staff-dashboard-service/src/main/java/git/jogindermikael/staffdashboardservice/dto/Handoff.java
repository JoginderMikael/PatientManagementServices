package git.jogindermikael.staffdashboardservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record Handoff(@NotNull UUID assigneeId, @NotBlank String queueRole,
        @NotNull Instant dueAt, @NotBlank String reason) {
}
