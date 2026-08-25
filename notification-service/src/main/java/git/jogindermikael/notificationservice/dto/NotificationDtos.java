package git.jogindermikael.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public final class NotificationDtos {
    private NotificationDtos() {
    }

    public record NotificationRequest(@NotNull UUID recipientId, @NotBlank String channel, @NotBlank String destination, @NotBlank String template, @NotBlank String body, UUID correlationId) {}
    public record AppointmentReminderRequest(@NotNull UUID patientId, @NotNull UUID appointmentId, @NotBlank String channel, @NotBlank String destination, @NotBlank String message) {}
    public record BillAlertRequest(@NotNull UUID patientId, @NotNull UUID invoiceId, @NotBlank String channel, @NotBlank String destination, @NotBlank String message) {}
    public record MfaCodeRequest(@NotNull UUID userId, @NotBlank String channel, @NotBlank String destination, @NotBlank String code) {}
}
