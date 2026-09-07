package git.jogindermikael.patientportalservice.controller;

import git.jogindermikael.patientportalservice.dto.PatientPortalDtos.*;
import git.jogindermikael.patientportalservice.model.PatientPortalModels.*;
import git.jogindermikael.patientportalservice.service.PatientPortalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
@RequestMapping("/portal")
@Tag(
    name = "Patient Portal",
    description = "Patient-facing access to records, appointments and bill payments")
public class PatientPortalController {
  private final PatientPortalService patientPortalService;

  public PatientPortalController(PatientPortalService patientPortalService) {
    this.patientPortalService = patientPortalService;
  }

  @GetMapping("/patients/{patientId}/overview")
  @Operation(summary = "Get a patient portal overview")
  public PortalOverview overview(@PathVariable UUID patientId) {
    return patientPortalService.overview(patientId);
  }

  @PostMapping("/appointment-requests")
  @Operation(summary = "Request an appointment from the patient portal")
  public ResponseEntity<PortalAppointmentRequest> requestAppointment(
      @Valid @RequestBody PortalAppointmentCommand request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(patientPortalService.requestAppointment(request));
  }

  @PostMapping("/record-requests")
  @Operation(summary = "Request access to patient records")
  public ResponseEntity<RecordAccessRequest> requestRecords(
      @Valid @RequestBody RecordAccessCommand request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(patientPortalService.requestRecords(request));
  }

  @PostMapping("/payments")
  @Operation(summary = "Submit a patient portal payment")
  public ResponseEntity<PortalPayment> payBill(
      @Valid @RequestBody PortalPaymentCommand request,
      @RequestHeader("Idempotency-Key") String reference) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(patientPortalService.payBill(request, reference));
  }
}
