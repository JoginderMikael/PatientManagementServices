package git.jogindermikael.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public record NotificationPreferenceRequest(@NotNull UUID recipientId, @NotBlank String timezone,
        LocalTime quietStart, LocalTime quietEnd, @NotBlank String locale, boolean optOut,
        @NotNull Set<String> allowedChannels) {
}
