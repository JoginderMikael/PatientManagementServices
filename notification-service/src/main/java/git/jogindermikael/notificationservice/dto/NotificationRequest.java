package git.jogindermikael.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record NotificationRequest(@NotNull UUID recipientId, @NotBlank String channel,
        @NotBlank String destination, @NotBlank String template, @NotBlank String body,
        UUID correlationId) {
}
