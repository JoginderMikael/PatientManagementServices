package git.jogindermikael.ehrservice.service;

import git.jogindermikael.ehrservice.dto.EhrDtos.*;
import git.jogindermikael.ehrservice.mapper.EhrMapper;
import git.jogindermikael.ehrservice.model.EhrModels.*;
import git.jogindermikael.ehrservice.repository.EhrRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EhrService {
    private final EhrRepository repository;
    private final EhrMapper mapper;

    public EhrService(EhrRepository repository, EhrMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public MedicalHistory recordHistory(MedicalHistoryRequest request) { return repository.saveHistory(mapper.toHistory(request)); }
    public List<MedicalHistory> historiesForPatient(UUID patientId) { return repository.findHistories().stream().filter(item -> item.patientId().equals(patientId)).toList(); }
    public Diagnosis recordDiagnosis(DiagnosisRequest request) { return repository.saveDiagnosis(mapper.toDiagnosis(request)); }
    public List<Diagnosis> diagnosesForPatient(UUID patientId) { return repository.findDiagnoses().stream().filter(item -> item.patientId().equals(patientId)).toList(); }
    public Prescription prescribe(PrescriptionRequest request) { return repository.savePrescription(mapper.toPrescription(request)); }
    public List<Prescription> prescriptionsForPatient(UUID patientId) { return repository.findPrescriptions().stream().filter(item -> item.patientId().equals(patientId)).toList(); }
    public LabResult addLabResult(LabResultRequest request) { return repository.saveLabResult(mapper.toLabResult(request)); }
    public LabResult importExternalLabResult(ExternalLabResultRequest request) { return repository.saveLabResult(mapper.toExternalLabResult(request)); }
    public List<LabResult> labResultsForPatient(UUID patientId) { return repository.findLabResults().stream().filter(item -> item.patientId().equals(patientId)).toList(); }
    public VaccinationRecord recordVaccination(VaccinationRequest request) { return repository.saveVaccination(mapper.toVaccination(request)); }
    public List<VaccinationRecord> vaccinationsForPatient(UUID patientId) { return repository.findVaccinations().stream().filter(item -> item.patientId().equals(patientId)).toList(); }
}
