package git.jogindermikael.appointmentservice.mapper;

import git.jogindermikael.appointmentservice.dto.*;
import git.jogindermikael.appointmentservice.model.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Component
public class AppointmentMapper {
    public DoctorSchedule toSchedule(DoctorScheduleRequest request) {
        UUID id = request.id() == null ? UUID.randomUUID() : request.id();
        LocalTime startsAt = LocalTime.parse(request.startsAt());
        LocalTime endsAt = LocalTime.parse(request.endsAt());
        if (!endsAt.isAfter(startsAt))
            throw new IllegalArgumentException("Schedule end must be after start");
        return new DoctorSchedule(id, request.doctorId(), request.workDate(), startsAt, endsAt, request.location());
    }

    public Appointment toAppointment(AppointmentRequest request) {
        int duration = request.durationMinutes() == null ? 30 : request.durationMinutes();
        return new Appointment(UUID.randomUUID(), request.patientId(), request.doctorId(), request.startsAt(),
                request.startsAt().plusMinutes(duration), request.reason(), "BOOKED", Instant.now());
    }

    public Appointment toCancelledAppointment(Appointment appointment, CancellationRequest request) {
        appointment.cancel(request.reason());
        return appointment;
    }

    public WaitlistEntry toWaitlistEntry(WaitlistRequest request) {
        return new WaitlistEntry(UUID.randomUUID(), request.patientId(), request.doctorId(), request.preferredDate(),
                request.reason(), Instant.now());
    }

    public VirtualConsultation toVirtualConsultation(Appointment appointment, VirtualConsultationRequest request) {
        return new VirtualConsultation(UUID.randomUUID(), appointment.id(), request.provider(), request.joinUrl(),
                "WAITING_ROOM_READY", Instant.now());
    }

    public ConsentForm toConsentForm(Appointment appointment, ConsentFormRequest request) {
        return new ConsentForm(UUID.randomUUID(), appointment.id(), request.patientId(), request.formType(),
                request.signature(), Instant.now());
    }
}
