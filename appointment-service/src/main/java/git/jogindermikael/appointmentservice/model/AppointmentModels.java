package git.jogindermikael.appointmentservice.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public final class AppointmentModels {
    private AppointmentModels() {
    }

    public record DoctorSchedule(UUID id, UUID doctorId, LocalDate workDate, String startsAt, String endsAt, String location) {}
    public record Appointment(UUID id, UUID patientId, UUID doctorId, LocalDateTime startsAt, String reason, String status, Instant updatedAt) {}
    public record WaitlistEntry(UUID id, UUID patientId, UUID doctorId, LocalDate preferredDate, String reason, Instant createdAt) {}
    public record VirtualConsultation(UUID id, UUID appointmentId, String provider, String joinUrl, String status, Instant createdAt) {}
    public record ConsentForm(UUID id, UUID appointmentId, UUID patientId, String formType, String signature, Instant signedAt) {}
}
