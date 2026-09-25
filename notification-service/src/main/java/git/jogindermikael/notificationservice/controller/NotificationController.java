package git.jogindermikael.notificationservice.controller;

import git.jogindermikael.notificationservice.dto.*;
import git.jogindermikael.notificationservice.model.NotificationMessage;
import git.jogindermikael.notificationservice.model.NotificationPreference;
import git.jogindermikael.notificationservice.model.NotificationTemplate;
import git.jogindermikael.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

@RestController
@PreAuthorize("hasAnyRole('ADMIN','CLINICIAN','REGISTRATION_STAFF','BILLING_STAFF')")
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
    public ResponseEntity<NotificationMessage> sendAppointmentReminder(
            @Valid @RequestBody AppointmentReminderRequest request) {
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

    @PostMapping("/templated")
    @Operation(summary = "Queue a localized templated notification")
    public ResponseEntity<NotificationMessage> sendTemplate(
            @Valid @RequestBody TemplateNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(notificationService.sendTemplate(request));
    }

    @PutMapping("/preferences/{recipientId}")
    @Operation(summary = "Create or replace recipient delivery preferences")
    public NotificationPreference savePreference(@PathVariable UUID recipientId,
            @Valid @RequestBody NotificationPreferenceRequest request) {
        if (!recipientId.equals(request.recipientId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Recipient id does not match request path");
        }
        return notificationService.savePreference(request);
    }

    @GetMapping("/preferences/{recipientId}")
    @Operation(summary = "Get recipient delivery preferences")
    public NotificationPreference preference(@PathVariable UUID recipientId) {
        return notificationService.preference(recipientId);
    }

    @PostMapping("/templates")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register a localized notification template")
    public ResponseEntity<NotificationTemplate> saveTemplate(
            @Valid @RequestBody NotificationTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.saveTemplate(request));
    }

    @PostMapping("/provider-callbacks")
    @Operation(summary = "Apply an authenticated provider delivery callback")
    public NotificationMessage providerCallback(@Valid @RequestBody DeliveryCallbackRequest request) {
        return notificationService.applyCallback(request);
    }

    @GetMapping("/dead-letters")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Inspect dead-lettered notifications with pagination")
    public Page<NotificationMessage> deadLetters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return notificationService.deadLetters(page, size);
    }

    @PostMapping("/{id}/replay")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Replay a failed or dead-lettered notification")
    public ResponseEntity<NotificationMessage> replay(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(notificationService.replay(id));
    }

    @GetMapping
    @Operation(summary = "List notifications")
    public List<NotificationMessage> list(@RequestParam(required = false) UUID recipientId) {
        return notificationService.list(recipientId);
    }
}
