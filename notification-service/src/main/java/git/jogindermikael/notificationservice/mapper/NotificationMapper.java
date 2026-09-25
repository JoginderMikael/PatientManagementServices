package git.jogindermikael.notificationservice.mapper;

import git.jogindermikael.notificationservice.dto.*;
import git.jogindermikael.notificationservice.model.NotificationMessage;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class NotificationMapper {
    public NotificationMessage fromNotificationRequest(NotificationRequest request) {
        return createMessage(request.recipientId(), request.channel(), request.destination(), request.template(), request.body(), request.correlationId());
    }

    public NotificationMessage fromAppointmentReminder(AppointmentReminderRequest request) {
        return createMessage(request.patientId(), request.channel(), request.destination(), "APPOINTMENT_REMINDER", request.message(), request.appointmentId());
    }

    public NotificationMessage fromBillAlert(BillAlertRequest request) {
        return createMessage(request.patientId(), request.channel(), request.destination(), "BILL_ALERT", request.message(), request.invoiceId());
    }

    public NotificationMessage fromMfaCode(MfaCodeRequest request) {
        return createMessage(request.userId(), request.channel(), request.destination(), "MFA_CODE", "Your verification code is " + request.code(), request.userId());
    }

    private NotificationMessage createMessage(UUID recipientId, String channel, String destination, String template, String body, UUID correlationId) {
        return new NotificationMessage(UUID.randomUUID(), recipientId, channel, destination, template, body, "QUEUED", correlationId, Instant.now());
    }
}
