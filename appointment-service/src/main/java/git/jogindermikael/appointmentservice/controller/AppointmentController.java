package git.jogindermikael.appointmentservice.controller;

import git.jogindermikael.appointmentservice.dto.*;
import git.jogindermikael.appointmentservice.model.*;
import git.jogindermikael.appointmentservice.service.AppointmentService;
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
@PreAuthorize("hasAnyRole('ADMIN','CLINICIAN','REGISTRATION_STAFF','PATIENT')")
@RequestMapping("/appointments")
@Tag(name = "Appointments", description = "Doctor schedules, patient appointments, cancellations, waitlists, telemedicine and consent forms")
public class AppointmentController {
    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/schedules")
    @Operation(summary = "List doctor schedules")
    public List<DoctorSchedule> listSchedules() {
        return appointmentService.listSchedules();
    }

    @PostMapping("/schedules")
    @Operation(summary = "Create or update a doctor schedule")
    public ResponseEntity<DoctorSchedule> upsertSchedule(@Valid @RequestBody DoctorScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.upsertSchedule(request));
    }

    @GetMapping
    @Operation(summary = "List appointments")
    public List<Appointment> listAppointments(@RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID doctorId) {
        return appointmentService.listAppointments(patientId, doctorId);
    }

    @GetMapping("/page")
    @Operation(summary = "List appointments with bounded pagination")
    public Page<Appointment> pageAppointments(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return appointmentService.pageAppointments(page, size);
    }

    @PostMapping
    @Operation(summary = "Book a patient appointment")
    public ResponseEntity<Appointment> bookAppointment(@Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.bookAppointment(request));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an appointment")
    public Appointment cancelAppointment(@PathVariable UUID id, @Valid @RequestBody CancellationRequest request) {
        return appointmentService.cancelAppointment(id, request);
    }

    @PostMapping("/{id}/reschedule")
    @Operation(summary = "Atomically reschedule an appointment and its resource reservations")
    public Appointment reschedule(@PathVariable UUID id, @Valid @RequestBody RescheduleRequest request) {
        return appointmentService.reschedule(id, request);
    }

    @PostMapping("/{id}/check-in")
    @Operation(summary = "Check a patient into an appointment")
    public Appointment checkIn(@PathVariable UUID id) {
        return appointmentService.checkIn(id);
    }

    @PostMapping("/{id}/check-out")
    @Operation(summary = "Complete a checked-in appointment")
    public Appointment checkOut(@PathVariable UUID id) {
        return appointmentService.checkOut(id);
    }

    @PostMapping("/{id}/no-show")
    @Operation(summary = "Mark an appointment as a no-show")
    public Appointment noShow(@PathVariable UUID id) {
        return appointmentService.markNoShow(id);
    }

    @PostMapping("/recurring")
    @Operation(summary = "Book an all-or-nothing recurring appointment series")
    public ResponseEntity<List<Appointment>> recurring(@Valid @RequestBody RecurringAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.bookRecurring(request));
    }

    @PostMapping("/resources")
    @PreAuthorize("hasAnyRole('ADMIN','REGISTRATION_STAFF')")
    @Operation(summary = "Register a location, room, or equipment resource")
    public ResponseEntity<AppointmentResource> registerResource(
            @Valid @RequestBody AppointmentResourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.registerResource(request));
    }

    @GetMapping("/resources")
    @Operation(summary = "List scheduling resources")
    public List<AppointmentResource> resources() {
        return appointmentService.listResources();
    }

    @PostMapping("/waitlist")
    @Operation(summary = "Add a patient to the appointment waitlist")
    public ResponseEntity<WaitlistEntry> joinWaitlist(@Valid @RequestBody WaitlistRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.joinWaitlist(request));
    }

    @GetMapping("/waitlist")
    @Operation(summary = "List waitlist entries")
    public List<WaitlistEntry> listWaitlist() {
        return appointmentService.listWaitlist();
    }

    @PostMapping("/waitlist/{id}/promote")
    @Operation(summary = "Atomically promote a waiting patient into an available slot")
    public Appointment promoteWaitlist(@PathVariable UUID id,
            @Valid @RequestBody WaitlistPromotionRequest request) {
        return appointmentService.promoteWaitlist(id, request);
    }

    @PostMapping("/{id}/virtual-consultations")
    @Operation(summary = "Create a telemedicine consultation room for an appointment")
    public ResponseEntity<VirtualConsultation> createVirtualConsultation(@PathVariable UUID id,
            @Valid @RequestBody VirtualConsultationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createVirtualConsultation(id, request));
    }

    @GetMapping("/virtual-consultations")
    @Operation(summary = "List virtual waiting rooms and consultations")
    public List<VirtualConsultation> listVirtualConsultations() {
        return appointmentService.listVirtualConsultations();
    }

    @PostMapping("/{id}/consent-forms")
    @Operation(summary = "Capture a digital consent form for an appointment")
    public ResponseEntity<ConsentForm> captureConsent(@PathVariable UUID id,
            @Valid @RequestBody ConsentFormRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.captureConsent(id, request));
    }

    @GetMapping("/consent-forms")
    @Operation(summary = "List digital consent forms")
    public List<ConsentForm> listConsentForms() {
        return appointmentService.listConsentForms();
    }
}
