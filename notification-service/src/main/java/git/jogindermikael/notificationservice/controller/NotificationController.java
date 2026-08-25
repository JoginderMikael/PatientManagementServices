package git.jogindermikael.notificationservice.controller;

import git.jogindermikael.notificationservice.dto.NotificationDtos.*;
import git.jogindermikael.notificationservice.model.NotificationMessage;
import git.jogindermikael.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "Email, SMS and push notification hub for reminders, bill alerts and MFA codes")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @Operation(summary = "Send a notification")
    public ResponseEntity<NotificationMessage> send(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(notificationService.send(request));
    }

    @PostMapping("/appointment-reminders")
    @Operation(summary = "Send an appointment reminder")
    public ResponseEntity<NotificationMessage> sendAppointmentReminder(@Valid @RequestBody AppointmentReminderRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(notificationService.sendAppointmentReminder(request));
    }

    @PostMapping("/bill-alerts")
    @Operation(summary = "Send a bill alert")
    public ResponseEntity<NotificationMessage> sendBillAlert(@Valid @RequestBody BillAlertRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(notificationService.sendBillAlert(request));
    }

    @PostMapping("/mfa-codes")
    @Operation(summary = "Send an MFA code")
    public ResponseEntity<NotificationMessage> sendMfaCode(@Valid @RequestBody MfaCodeRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(notificationService.sendMfaCode(request));
    }

    @GetMapping
    @Operation(summary = "List notifications")
    public List<NotificationMessage> list(@RequestParam(required = false) UUID recipientId) {
        return notificationService.list(recipientId);
    }
}
