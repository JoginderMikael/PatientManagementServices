package git.jogindermikael.appointmentservice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class AppointmentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppointmentServiceApplication.class, args);
    }
}

@RestController
@RequestMapping("/appointments")
@Tag(name = "Appointments", description = "Doctor schedules, patient appointments, cancellations, waitlists, telemedicine and consent forms")
class AppointmentController {
    private final Map<UUID, DoctorSchedule> schedules = new ConcurrentHashMap<>();
    private final Map<UUID, Appointment> appointments = new ConcurrentHashMap<>();
    private final Map<UUID, WaitlistEntry> waitlist = new ConcurrentHashMap<>();
    private final Map<UUID, VirtualConsultation> consultations = new ConcurrentHashMap<>();
    private final Map<UUID, ConsentForm> consentForms = new ConcurrentHashMap<>();

    @GetMapping("/schedules")
    @Operation(summary = "List doctor schedules")
    List<DoctorSchedule> listSchedules() {
        return schedules.values().stream().sorted(Comparator.comparing(DoctorSchedule::workDate)).toList();
    }

    @PostMapping("/schedules")
    @Operation(summary = "Create or update a doctor schedule")
    ResponseEntity<DoctorSchedule> upsertSchedule(@Valid @RequestBody DoctorScheduleRequest request) {
        UUID id = request.id() == null ? UUID.randomUUID() : request.id();
        DoctorSchedule schedule = new DoctorSchedule(id, request.doctorId(), request.workDate(), request.startsAt(), request.endsAt(), request.location());
        schedules.put(id, schedule);
        return ResponseEntity.status(HttpStatus.CREATED).body(schedule);
    }

    @GetMapping
    @Operation(summary = "List appointments")
    List<Appointment> listAppointments(@RequestParam(required = false) UUID patientId, @RequestParam(required = false) UUID doctorId) {
        return appointments.values().stream()
                .filter(appointment -> patientId == null || appointment.patientId().equals(patientId))
                .filter(appointment -> doctorId == null || appointment.doctorId().equals(doctorId))
                .sorted(Comparator.comparing(Appointment::startsAt))
                .toList();
    }

    @PostMapping
    @Operation(summary = "Book a patient appointment")
    ResponseEntity<Appointment> bookAppointment(@Valid @RequestBody AppointmentRequest request) {
        UUID id = UUID.randomUUID();
        Appointment appointment = new Appointment(id, request.patientId(), request.doctorId(), request.startsAt(), request.reason(), "BOOKED", Instant.now());
        appointments.put(id, appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an appointment")
    Appointment cancelAppointment(@PathVariable UUID id, @Valid @RequestBody CancellationRequest request) {
        Appointment appointment = getAppointment(id);
        Appointment cancelled = new Appointment(appointment.id(), appointment.patientId(), appointment.doctorId(), appointment.startsAt(), request.reason(), "CANCELLED", Instant.now());
        appointments.put(id, cancelled);
        return cancelled;
    }

    @PostMapping("/waitlist")
    @Operation(summary = "Add a patient to the appointment waitlist")
    ResponseEntity<WaitlistEntry> joinWaitlist(@Valid @RequestBody WaitlistRequest request) {
        UUID id = UUID.randomUUID();
        WaitlistEntry entry = new WaitlistEntry(id, request.patientId(), request.doctorId(), request.preferredDate(), request.reason(), Instant.now());
        waitlist.put(id, entry);
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    @GetMapping("/waitlist")
    @Operation(summary = "List waitlist entries")
    List<WaitlistEntry> listWaitlist() {
        return waitlist.values().stream().sorted(Comparator.comparing(WaitlistEntry::createdAt)).toList();
    }

    @PostMapping("/{id}/virtual-consultations")
    @Operation(summary = "Create a telemedicine consultation room for an appointment")
    ResponseEntity<VirtualConsultation> createVirtualConsultation(@PathVariable UUID id, @Valid @RequestBody VirtualConsultationRequest request) {
        Appointment appointment = getAppointment(id);
        UUID consultationId = UUID.randomUUID();
        VirtualConsultation consultation = new VirtualConsultation(consultationId, appointment.id(), request.provider(), request.joinUrl(), "WAITING_ROOM_READY", Instant.now());
        consultations.put(consultationId, consultation);
        return ResponseEntity.status(HttpStatus.CREATED).body(consultation);
    }

    @GetMapping("/virtual-consultations")
    @Operation(summary = "List virtual waiting rooms and consultations")
    List<VirtualConsultation> listVirtualConsultations() {
        return consultations.values().stream().sorted(Comparator.comparing(VirtualConsultation::createdAt)).toList();
    }

    @PostMapping("/{id}/consent-forms")
    @Operation(summary = "Capture a digital consent form for an appointment")
    ResponseEntity<ConsentForm> captureConsent(@PathVariable UUID id, @Valid @RequestBody ConsentFormRequest request) {
        Appointment appointment = getAppointment(id);
        UUID consentId = UUID.randomUUID();
        ConsentForm form = new ConsentForm(consentId, appointment.id(), request.patientId(), request.formType(), request.signature(), Instant.now());
        consentForms.put(consentId, form);
        return ResponseEntity.status(HttpStatus.CREATED).body(form);
    }

    @GetMapping("/consent-forms")
    @Operation(summary = "List digital consent forms")
    List<ConsentForm> listConsentForms() {
        return consentForms.values().stream().sorted(Comparator.comparing(ConsentForm::signedAt)).toList();
    }

    private Appointment getAppointment(UUID id) {
        Appointment appointment = appointments.get(id);
        if (appointment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found");
        }
        return appointment;
    }
}

record DoctorSchedule(UUID id, UUID doctorId, LocalDate workDate, String startsAt, String endsAt, String location) {}
record Appointment(UUID id, UUID patientId, UUID doctorId, LocalDateTime startsAt, String reason, String status, Instant updatedAt) {}
record WaitlistEntry(UUID id, UUID patientId, UUID doctorId, LocalDate preferredDate, String reason, Instant createdAt) {}
record VirtualConsultation(UUID id, UUID appointmentId, String provider, String joinUrl, String status, Instant createdAt) {}
record ConsentForm(UUID id, UUID appointmentId, UUID patientId, String formType, String signature, Instant signedAt) {}

record DoctorScheduleRequest(UUID id, @NotNull UUID doctorId, @NotNull LocalDate workDate, @NotBlank String startsAt, @NotBlank String endsAt, @NotBlank String location) {}
record AppointmentRequest(@NotNull UUID patientId, @NotNull UUID doctorId, @NotNull @FutureOrPresent LocalDateTime startsAt, @NotBlank String reason) {}
record CancellationRequest(@NotBlank String reason) {}
record WaitlistRequest(@NotNull UUID patientId, @NotNull UUID doctorId, @NotNull LocalDate preferredDate, @NotBlank String reason) {}
record VirtualConsultationRequest(@NotBlank String provider, @NotBlank String joinUrl) {}
record ConsentFormRequest(@NotNull UUID patientId, @NotBlank String formType, @NotBlank String signature) {}
