package git.jogindermikael.patientportalservice.service;

import git.jogindermikael.patientportalservice.dto.PatientPortalDtos.*;
import git.jogindermikael.patientportalservice.mapper.PatientPortalMapper;
import git.jogindermikael.patientportalservice.model.PatientPortalModels.*;
import git.jogindermikael.patientportalservice.repository.PatientPortalRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PatientPortalService {
    private final PatientPortalRepository repository;
    private final PatientPortalMapper mapper;

    public PatientPortalService(PatientPortalRepository repository, PatientPortalMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public PortalOverview overview(UUID patientId) {
        return new PortalOverview(patientId, "ACTIVE",
                repository.findAppointmentRequests().stream().filter(item -> item.patientId().equals(patientId)).toList(),
                repository.findRecordRequests().stream().filter(item -> item.patientId().equals(patientId)).toList(),
                repository.findPayments().stream().filter(item -> item.patientId().equals(patientId)).toList());
    }

    public PortalAppointmentRequest requestAppointment(PortalAppointmentCommand request) { return repository.saveAppointmentRequest(mapper.toAppointmentRequest(request)); }
    public RecordAccessRequest requestRecords(RecordAccessCommand request) { return repository.saveRecordRequest(mapper.toRecordAccessRequest(request)); }
    public PortalPayment payBill(PortalPaymentCommand request) { return repository.savePayment(mapper.toPayment(request)); }
}
