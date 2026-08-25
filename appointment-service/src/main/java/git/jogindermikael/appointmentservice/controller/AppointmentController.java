package git.jogindermikael.appointmentservice.controller;

import git.jogindermikael.appointmentservice.dto.AppointmentDtos.*;
import git.jogindermikael.appointmentservice.model.AppointmentModels.*;
import git.jogindermikael.appointmentservice.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
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
    public List<Appointment> listAppointments(@RequestParam(required = false) UUID patientId, @RequestParam(required = false) UUID doctorId) {
        return appointmentService.listAppointments(patientId, doctorId);
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

    @PostMapping("/{id}/virtual-consultations")
    @Operation(summary = "Create a telemedicine consultation room for an appointment")
    public ResponseEntity<VirtualConsultation> createVirtualConsultation(@PathVariable UUID id, @Valid @RequestBody VirtualConsultationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createVirtualConsultation(id, request));
    }

    @GetMapping("/virtual-consultations")
    @Operation(summary = "List virtual waiting rooms and consultations")
    public List<VirtualConsultation> listVirtualConsultations() {
        return appointmentService.listVirtualConsultations();
    }

    @PostMapping("/{id}/consent-forms")
    @Operation(summary = "Capture a digital consent form for an appointment")
    public ResponseEntity<ConsentForm> captureConsent(@PathVariable UUID id, @Valid @RequestBody ConsentFormRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.captureConsent(id, request));
    }

    @GetMapping("/consent-forms")
    @Operation(summary = "List digital consent forms")
    public List<ConsentForm> listConsentForms() {
        return appointmentService.listConsentForms();
    }
}
