package git.jogindermikael.auditcomplianceservice.controller;

import git.jogindermikael.auditcomplianceservice.dto.AuditEventRequest;
import git.jogindermikael.auditcomplianceservice.model.AuditEvent;
import git.jogindermikael.auditcomplianceservice.service.AuditComplianceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
@RequestMapping("/audit")
@Tag(name = "Audit & Compliance", description = "HIPAA-oriented audit trail for patient data access and mutations")
public class AuditComplianceController {
    private final AuditComplianceService auditComplianceService;

    public AuditComplianceController(AuditComplianceService auditComplianceService) {
        this.auditComplianceService = auditComplianceService;
    }

    @PostMapping("/events")
    @Operation(summary = "Record an audit event")
    public ResponseEntity<AuditEvent> recordEvent(@Valid @RequestBody AuditEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auditComplianceService.recordEvent(request));
    }

    @GetMapping("/events")
    @Operation(summary = "Search audit events")
    public List<AuditEvent> searchEvents(@RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID actorId) {
        return auditComplianceService.searchEvents(patientId, actorId);
    }

    @GetMapping("/patient-record-access/{patientId}")
    @Operation(summary = "Show who viewed or changed a patient record and when")
    public List<AuditEvent> patientRecordAccess(@PathVariable UUID patientId) {
        return auditComplianceService.patientRecordAccess(patientId);
    }
}
