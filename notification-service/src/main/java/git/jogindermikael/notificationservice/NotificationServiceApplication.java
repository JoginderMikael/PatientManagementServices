package git.jogindermikael.notificationservice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}

@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "Email, SMS and push notification hub for reminders, bill alerts and MFA codes")
class NotificationController {
    private final Map<UUID, NotificationMessage> messages = new ConcurrentHashMap<>();

    @PostMapping
    @Operation(summary = "Send a notification")
    ResponseEntity<NotificationMessage> send(@Valid @RequestBody NotificationRequest request) {
        NotificationMessage message = createMessage(request.recipientId(), request.channel(), request.destination(), request.template(), request.body(), request.correlationId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(message);
    }

    @PostMapping("/appointment-reminders")
    @Operation(summary = "Send an appointment reminder")
    ResponseEntity<NotificationMessage> sendAppointmentReminder(@Valid @RequestBody AppointmentReminderRequest request) {
        NotificationMessage message = createMessage(request.patientId(), request.channel(), request.destination(), "APPOINTMENT_REMINDER", request.message(), request.appointmentId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(message);
    }

    @PostMapping("/bill-alerts")
    @Operation(summary = "Send a bill alert")
    ResponseEntity<NotificationMessage> sendBillAlert(@Valid @RequestBody BillAlertRequest request) {
        NotificationMessage message = createMessage(request.patientId(), request.channel(), request.destination(), "BILL_ALERT", request.message(), request.invoiceId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(message);
    }

    @PostMapping("/mfa-codes")
    @Operation(summary = "Send an MFA code")
    ResponseEntity<NotificationMessage> sendMfaCode(@Valid @RequestBody MfaCodeRequest request) {
        NotificationMessage message = createMessage(request.userId(), request.channel(), request.destination(), "MFA_CODE", "Your verification code is " + request.code(), request.userId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(message);
    }

    @GetMapping
    @Operation(summary = "List notifications")
    List<NotificationMessage> list(@RequestParam(required = false) UUID recipientId) {
        return messages.values().stream()
                .filter(message -> recipientId == null || message.recipientId().equals(recipientId))
                .sorted(Comparator.comparing(NotificationMessage::createdAt))
                .toList();
    }

    private NotificationMessage createMessage(UUID recipientId, String channel, String destination, String template, String body, UUID correlationId) {
        UUID id = UUID.randomUUID();
        NotificationMessage message = new NotificationMessage(id, recipientId, channel, destination, template, body, "QUEUED", correlationId, Instant.now());
        messages.put(id, message);
        return message;
    }
}

record NotificationMessage(UUID id, UUID recipientId, String channel, String destination, String template, String body, String status, UUID correlationId, Instant createdAt) {}
record NotificationRequest(@NotNull UUID recipientId, @NotBlank String channel, @NotBlank String destination, @NotBlank String template, @NotBlank String body, UUID correlationId) {}
record AppointmentReminderRequest(@NotNull UUID patientId, @NotNull UUID appointmentId, @NotBlank String channel, @NotBlank String destination, @NotBlank String message) {}
record BillAlertRequest(@NotNull UUID patientId, @NotNull UUID invoiceId, @NotBlank String channel, @NotBlank String destination, @NotBlank String message) {}
record MfaCodeRequest(@NotNull UUID userId, @NotBlank String channel, @NotBlank String destination, @NotBlank String code) {}
