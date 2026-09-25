package git.jogindermikael.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record TemplateNotificationRequest(@NotNull UUID recipientId, @NotBlank String destination,
        @NotBlank String templateKey, String locale, @NotBlank String channel,
        @NotNull Map<String, String> variables, UUID correlationId) {
}
