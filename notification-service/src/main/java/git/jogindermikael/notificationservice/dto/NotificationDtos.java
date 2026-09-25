package git.jogindermikael.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;

public final class NotificationDtos {
    private NotificationDtos() {
    }

    public record NotificationRequest(@NotNull UUID recipientId, @NotBlank String channel, @NotBlank String destination,
            @NotBlank String template, @NotBlank String body, UUID correlationId) {
    }

    public record AppointmentReminderRequest(@NotNull UUID patientId, @NotNull UUID appointmentId,
            @NotBlank String channel, @NotBlank String destination, @NotBlank String message) {
    }

    public record BillAlertRequest(@NotNull UUID patientId, @NotNull UUID invoiceId, @NotBlank String channel,
            @NotBlank String destination, @NotBlank String message) {
    }

    public record MfaCodeRequest(@NotNull UUID userId, @NotBlank String channel, @NotBlank String destination,
            @NotBlank String code) {
    }

    public record NotificationPreferenceRequest(@NotNull UUID recipientId, @NotBlank String timezone,
            LocalTime quietStart, LocalTime quietEnd, @NotBlank String locale, boolean optOut,
            @NotNull Set<String> allowedChannels) {
    }

    public record NotificationTemplateRequest(@NotBlank String templateKey, @NotBlank String locale,
            @NotBlank String channel, String subject, @NotBlank String body) {
    }

    public record TemplateNotificationRequest(@NotNull UUID recipientId, @NotBlank String destination,
            @NotBlank String templateKey, String locale, @NotBlank String channel,
            @NotNull Map<String, String> variables, UUID correlationId) {
    }

    public record DeliveryCallbackRequest(@NotBlank String providerMessageId,
            @NotBlank String status, String detail) {
    }
}
