package git.jogindermikael.appointmentservice.repository;

import git.jogindermikael.appointmentservice.model.AppointmentModels.*;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AppointmentRepository {
    private final ConcurrentHashMap<UUID, DoctorSchedule> schedules = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Appointment> appointments = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, WaitlistEntry> waitlist = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, VirtualConsultation> consultations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ConsentForm> consentForms = new ConcurrentHashMap<>();

    public DoctorSchedule saveSchedule(DoctorSchedule schedule) {
        schedules.put(schedule.id(), schedule);
        return schedule;
    }

    public Collection<DoctorSchedule> findSchedules() {
        return schedules.values();
    }

    public Appointment saveAppointment(Appointment appointment) {
        appointments.put(appointment.id(), appointment);
        return appointment;
    }

    public Optional<Appointment> findAppointmentById(UUID id) {
        return Optional.ofNullable(appointments.get(id));
    }

    public Collection<Appointment> findAppointments() {
        return appointments.values();
    }

    public WaitlistEntry saveWaitlistEntry(WaitlistEntry entry) {
        waitlist.put(entry.id(), entry);
        return entry;
    }

    public Collection<WaitlistEntry> findWaitlist() {
        return waitlist.values();
    }

    public VirtualConsultation saveVirtualConsultation(VirtualConsultation consultation) {
        consultations.put(consultation.id(), consultation);
        return consultation;
    }

    public Collection<VirtualConsultation> findVirtualConsultations() {
        return consultations.values();
    }

    public ConsentForm saveConsentForm(ConsentForm form) {
        consentForms.put(form.id(), form);
        return form;
    }

    public Collection<ConsentForm> findConsentForms() {
        return consentForms.values();
    }
}
