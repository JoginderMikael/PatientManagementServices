package git.jogindermikael.patientportalservice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class PatientPortalServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PatientPortalServiceApplication.class, args);
    }
}

@RestController
@RequestMapping("/portal")
@Tag(name = "Patient Portal", description = "Patient-facing access to records, appointments and bill payments")
class PatientPortalController {
    private final Map<UUID, PortalAppointmentRequest> appointmentRequests = new ConcurrentHashMap<>();
    private final Map<UUID, RecordAccessRequest> recordRequests = new ConcurrentHashMap<>();
    private final Map<UUID, PortalPayment> payments = new ConcurrentHashMap<>();

    @GetMapping("/patients/{patientId}/overview")
    @Operation(summary = "Get a patient portal overview")
    PortalOverview overview(@PathVariable UUID patientId) {
        return new PortalOverview(patientId, "ACTIVE", appointmentRequests.values().stream().filter(item -> item.patientId().equals(patientId)).toList(), recordRequests.values().stream().filter(item -> item.patientId().equals(patientId)).toList(), payments.values().stream().filter(item -> item.patientId().equals(patientId)).toList());
    }

    @PostMapping("/appointment-requests")
    @Operation(summary = "Request an appointment from the patient portal")
    ResponseEntity<PortalAppointmentRequest> requestAppointment(@Valid @RequestBody PortalAppointmentCommand request) {
        UUID id = UUID.randomUUID();
        PortalAppointmentRequest appointmentRequest = new PortalAppointmentRequest(id, request.patientId(), request.preferredSpecialty(), request.reason(), "REQUESTED", Instant.now());
        appointmentRequests.put(id, appointmentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentRequest);
    }

    @PostMapping("/record-requests")
    @Operation(summary = "Request access to patient records")
    ResponseEntity<RecordAccessRequest> requestRecords(@Valid @RequestBody RecordAccessCommand request) {
        UUID id = UUID.randomUUID();
        RecordAccessRequest recordRequest = new RecordAccessRequest(id, request.patientId(), request.recordType(), "READY_FOR_REVIEW", Instant.now());
        recordRequests.put(id, recordRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(recordRequest);
    }

    @PostMapping("/payments")
    @Operation(summary = "Submit a patient portal payment")
    ResponseEntity<PortalPayment> payBill(@Valid @RequestBody PortalPaymentCommand request) {
        UUID id = UUID.randomUUID();
        PortalPayment payment = new PortalPayment(id, request.patientId(), request.invoiceId(), request.amount(), "SUBMITTED", Instant.now());
        payments.put(id, payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }
}

record PortalOverview(UUID patientId, String accountStatus, List<PortalAppointmentRequest> appointments, List<RecordAccessRequest> recordRequests, List<PortalPayment> payments) {}
record PortalAppointmentRequest(UUID id, UUID patientId, String preferredSpecialty, String reason, String status, Instant createdAt) {}
record RecordAccessRequest(UUID id, UUID patientId, String recordType, String status, Instant createdAt) {}
record PortalPayment(UUID id, UUID patientId, UUID invoiceId, BigDecimal amount, String status, Instant createdAt) {}

record PortalAppointmentCommand(@NotNull UUID patientId, @NotBlank String preferredSpecialty, @NotBlank String reason) {}
record RecordAccessCommand(@NotNull UUID patientId, @NotBlank String recordType) {}
record PortalPaymentCommand(@NotNull UUID patientId, @NotNull UUID invoiceId, @NotNull @DecimalMin("0.00") BigDecimal amount) {}
