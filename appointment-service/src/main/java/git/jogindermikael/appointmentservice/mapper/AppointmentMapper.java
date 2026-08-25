package git.jogindermikael.appointmentservice.mapper;

import git.jogindermikael.appointmentservice.dto.AppointmentDtos.*;
import git.jogindermikael.appointmentservice.model.AppointmentModels.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class AppointmentMapper {
    public DoctorSchedule toSchedule(DoctorScheduleRequest request) {
        UUID id = request.id() == null ? UUID.randomUUID() : request.id();
        return new DoctorSchedule(id, request.doctorId(), request.workDate(), request.startsAt(), request.endsAt(), request.location());
    }

    public Appointment toAppointment(AppointmentRequest request) {
        return new Appointment(UUID.randomUUID(), request.patientId(), request.doctorId(), request.startsAt(), request.reason(), "BOOKED", Instant.now());
    }

    public Appointment toCancelledAppointment(Appointment appointment, CancellationRequest request) {
        return new Appointment(appointment.id(), appointment.patientId(), appointment.doctorId(), appointment.startsAt(), request.reason(), "CANCELLED", Instant.now());
    }

    public WaitlistEntry toWaitlistEntry(WaitlistRequest request) {
        return new WaitlistEntry(UUID.randomUUID(), request.patientId(), request.doctorId(), request.preferredDate(), request.reason(), Instant.now());
    }

    public VirtualConsultation toVirtualConsultation(Appointment appointment, VirtualConsultationRequest request) {
        return new VirtualConsultation(UUID.randomUUID(), appointment.id(), request.provider(), request.joinUrl(), "WAITING_ROOM_READY", Instant.now());
    }

    public ConsentForm toConsentForm(Appointment appointment, ConsentFormRequest request) {
        return new ConsentForm(UUID.randomUUID(), appointment.id(), request.patientId(), request.formType(), request.signature(), Instant.now());
    }
}
