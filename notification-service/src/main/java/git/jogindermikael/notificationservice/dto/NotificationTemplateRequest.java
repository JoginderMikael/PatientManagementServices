package git.jogindermikael.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationTemplateRequest(@NotBlank String templateKey, @NotBlank String locale,
        @NotBlank String channel, String subject, @NotBlank String body) {
}
