package git.jogindermikael.auditcomplianceservice;

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
public class AuditComplianceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuditComplianceServiceApplication.class, args);
    }
}

@RestController
@RequestMapping("/audit")
@Tag(name = "Audit & Compliance", description = "HIPAA-oriented audit trail for patient data access and mutations")
class AuditComplianceController {
    private final Map<UUID, AuditEvent> events = new ConcurrentHashMap<>();

    @PostMapping("/events")
    @Operation(summary = "Record an audit event")
    ResponseEntity<AuditEvent> recordEvent(@Valid @RequestBody AuditEventRequest request) {
        UUID id = UUID.randomUUID();
        AuditEvent event = new AuditEvent(id, request.actorId(), request.actorRole(), request.action(), request.patientId(), request.resourceType(), request.resourceId(), request.sourceService(), request.reason(), Instant.now());
        events.put(id, event);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @GetMapping("/events")
    @Operation(summary = "Search audit events")
    List<AuditEvent> searchEvents(@RequestParam(required = false) UUID patientId, @RequestParam(required = false) UUID actorId) {
        return events.values().stream()
                .filter(event -> patientId == null || event.patientId().equals(patientId))
                .filter(event -> actorId == null || event.actorId().equals(actorId))
                .sorted(Comparator.comparing(AuditEvent::occurredAt))
                .toList();
    }

    @GetMapping("/patient-record-access/{patientId}")
    @Operation(summary = "Show who viewed or changed a patient record and when")
    List<AuditEvent> patientRecordAccess(@PathVariable UUID patientId) {
        return events.values().stream()
                .filter(event -> event.patientId().equals(patientId))
                .filter(event -> event.action().contains("VIEW") || event.action().contains("READ") || event.action().contains("UPDATE"))
                .sorted(Comparator.comparing(AuditEvent::occurredAt))
                .toList();
    }
}

record AuditEvent(UUID id, UUID actorId, String actorRole, String action, UUID patientId, String resourceType, UUID resourceId, String sourceService, String reason, Instant occurredAt) {}
record AuditEventRequest(@NotNull UUID actorId, @NotBlank String actorRole, @NotBlank String action, @NotNull UUID patientId, @NotBlank String resourceType, @NotNull UUID resourceId, @NotBlank String sourceService, @NotBlank String reason) {}
