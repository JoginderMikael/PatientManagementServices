package git.jogindermikael.appointmentservice.repository;

import git.jogindermikael.appointmentservice.model.AppointmentModels.*;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AppointmentRepository {
    private final DoctorScheduleJpaRepository schedules; private final AppointmentJpaRepository appointments;
    private final WaitlistRepository waitlist; private final VirtualConsultationRepository consultations; private final ConsentFormRepository consents;
    public AppointmentRepository(DoctorScheduleJpaRepository schedules, AppointmentJpaRepository appointments, WaitlistRepository waitlist, VirtualConsultationRepository consultations, ConsentFormRepository consents){this.schedules=schedules;this.appointments=appointments;this.waitlist=waitlist;this.consultations=consultations;this.consents=consents;}
    public DoctorSchedule saveSchedule(DoctorSchedule value){return schedules.save(value);} public Collection<DoctorSchedule> findSchedules(){return schedules.findAll();}
    public Appointment saveAppointment(Appointment value){return appointments.save(value);} public Optional<Appointment> findAppointmentById(UUID id){return appointments.findById(id);} public Collection<Appointment> findAppointments(){return appointments.findAll();}
    public WaitlistEntry saveWaitlistEntry(WaitlistEntry value){return waitlist.save(value);} public Collection<WaitlistEntry> findWaitlist(){return waitlist.findAll();}
    public VirtualConsultation saveVirtualConsultation(VirtualConsultation value){return consultations.save(value);} public Collection<VirtualConsultation> findVirtualConsultations(){return consultations.findAll();}
    public ConsentForm saveConsentForm(ConsentForm value){return consents.save(value);} public Collection<ConsentForm> findConsentForms(){return consents.findAll();}
}
