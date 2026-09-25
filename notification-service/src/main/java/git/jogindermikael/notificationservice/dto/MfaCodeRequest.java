package git.jogindermikael.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MfaCodeRequest(@NotNull UUID userId, @NotBlank String channel,
        @NotBlank String destination, @NotBlank String code) {
}
