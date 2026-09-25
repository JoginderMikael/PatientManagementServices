package git.jogindermikael.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AppointmentReminderRequest(@NotNull UUID patientId, @NotNull UUID appointmentId,
        @NotBlank String channel, @NotBlank String destination, @NotBlank String message) {
}
