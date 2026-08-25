package git.jogindermikael.appointmentservice.service;

import git.jogindermikael.appointmentservice.dto.AppointmentDtos.*;
import git.jogindermikael.appointmentservice.mapper.AppointmentMapper;
import git.jogindermikael.appointmentservice.model.AppointmentModels.*;
import git.jogindermikael.appointmentservice.repository.AppointmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {
    private final AppointmentRepository repository;
    private final AppointmentMapper mapper;

    public AppointmentService(AppointmentRepository repository, AppointmentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<DoctorSchedule> listSchedules() {
        return repository.findSchedules().stream().sorted(Comparator.comparing(DoctorSchedule::workDate)).toList();
    }

    public DoctorSchedule upsertSchedule(DoctorScheduleRequest request) {
        return repository.saveSchedule(mapper.toSchedule(request));
    }

    public List<Appointment> listAppointments(UUID patientId, UUID doctorId) {
        return repository.findAppointments().stream()
                .filter(appointment -> patientId == null || appointment.patientId().equals(patientId))
                .filter(appointment -> doctorId == null || appointment.doctorId().equals(doctorId))
                .sorted(Comparator.comparing(Appointment::startsAt))
                .toList();
    }

    public Appointment bookAppointment(AppointmentRequest request) {
        return repository.saveAppointment(mapper.toAppointment(request));
    }

    public Appointment cancelAppointment(UUID id, CancellationRequest request) {
        Appointment appointment = getAppointment(id);
        return repository.saveAppointment(mapper.toCancelledAppointment(appointment, request));
    }

    public WaitlistEntry joinWaitlist(WaitlistRequest request) {
        return repository.saveWaitlistEntry(mapper.toWaitlistEntry(request));
    }

    public List<WaitlistEntry> listWaitlist() {
        return repository.findWaitlist().stream().sorted(Comparator.comparing(WaitlistEntry::createdAt)).toList();
    }

    public VirtualConsultation createVirtualConsultation(UUID appointmentId, VirtualConsultationRequest request) {
        return repository.saveVirtualConsultation(mapper.toVirtualConsultation(getAppointment(appointmentId), request));
    }

    public List<VirtualConsultation> listVirtualConsultations() {
        return repository.findVirtualConsultations().stream().sorted(Comparator.comparing(VirtualConsultation::createdAt)).toList();
    }

    public ConsentForm captureConsent(UUID appointmentId, ConsentFormRequest request) {
        return repository.saveConsentForm(mapper.toConsentForm(getAppointment(appointmentId), request));
    }

    public List<ConsentForm> listConsentForms() {
        return repository.findConsentForms().stream().sorted(Comparator.comparing(ConsentForm::signedAt)).toList();
    }

    private Appointment getAppointment(UUID id) {
        return repository.findAppointmentById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }
}
