package git.jogindermikael.patientservice.service;

import git.jogindermikael.patientservice.dto.PatientRequestDTO;
import git.jogindermikael.patientservice.dto.PatientResponseDTO;
import git.jogindermikael.patientservice.exception.EmailAlreadyExistsException;
import git.jogindermikael.patientservice.exception.PatientNotFoundException;
import git.jogindermikael.patientservice.mapper.PatientMapper;
import git.jogindermikael.patientservice.model.Patient;
import git.jogindermikael.patientservice.model.PatientMergeHistory;
import git.jogindermikael.patientservice.model.PatientStatus;
import git.jogindermikael.patientservice.repository.PatientMergeHistoryRepository;
import git.jogindermikael.patientservice.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final PatientMergeHistoryRepository mergeHistoryRepository;
    private final PatientOutboxService outbox;

    public PatientService(PatientRepository patientRepository,
                          PatientMergeHistoryRepository mergeHistoryRepository,
                          PatientOutboxService outbox) {
        this.patientRepository = patientRepository;
        this.mergeHistoryRepository = mergeHistoryRepository;
        this.outbox = outbox;
    }

    @Transactional(readOnly = true)
    public List<PatientResponseDTO> getPatients() {
        return patientRepository.findAll().stream().map(PatientMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public PatientResponseDTO getPatient(UUID id) { return PatientMapper.toDto(requirePatient(id)); }

    @Transactional
    public PatientResponseDTO createPatient(PatientRequestDTO request, String registrationKey) {
        if (registrationKey != null && !registrationKey.isBlank()) {
            Patient existing = patientRepository.findByRegistrationKey(registrationKey.trim()).orElse(null);
            if (existing != null) return PatientMapper.toDto(existing);
        }
        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("A patient with this email already exists " + request.getEmail());
        }
        Patient patient = PatientMapper.toModel(request);
        patient.setMrn(generateMrn());
        patient.setRegistrationKey(registrationKey == null || registrationKey.isBlank() ? null : registrationKey.trim());
        patient.setStatus(PatientStatus.ACTIVE);
        Patient saved = patientRepository.saveAndFlush(patient);
        outbox.append("PATIENT_REGISTERED", saved);
        return PatientMapper.toDto(saved);
    }

    public PatientResponseDTO createPatient(PatientRequestDTO request) { return createPatient(request, null); }

    @Transactional
    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO request) {
        Patient patient = requirePatient(id);
        ensureMutable(patient);
        if (patientRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new EmailAlreadyExistsException("A patient with this email already exists " + request.getEmail());
        }
        patient.setName(request.getName());
        patient.setEmail(request.getEmail());
        patient.setAddress(request.getAddress());
        patient.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
        patient.setPhone(request.getPhone());
        patient.setGender(request.getGender());
        patient.setPreferredLanguage(request.getPreferredLanguage());
        patient.setExternalIdentifiers(request.getExternalIdentifiers());
        Patient saved = patientRepository.save(patient);
        outbox.append("PATIENT_UPDATED", saved);
        return PatientMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<PatientResponseDTO> findDuplicates(String name, LocalDate dateOfBirth, String phone, String email) {
        Map<UUID, Patient> candidates = new LinkedHashMap<>();
        if (name != null && !name.isBlank() && dateOfBirth != null) {
            patientRepository.findByNameContainingIgnoreCaseAndDateOfBirth(name.trim(), dateOfBirth)
                    .forEach(patient -> candidates.put(patient.getId(), patient));
        }
        if (phone != null && !phone.isBlank()) patientRepository.findByPhone(phone).forEach(patient -> candidates.put(patient.getId(), patient));
        if (email != null && !email.isBlank()) patientRepository.findByEmailIgnoreCase(email).forEach(patient -> candidates.put(patient.getId(), patient));
        return candidates.values().stream().map(PatientMapper::toDto).toList();
    }

    @Transactional
    public PatientResponseDTO changeStatus(UUID id, PatientStatus status) {
        Patient patient = requirePatient(id);
        if (patient.getStatus() == PatientStatus.MERGED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Merged patients must be unmerged before changing status");
        }
        patient.setStatus(status);
        outbox.append("PATIENT_STATUS_CHANGED", patient);
        return PatientMapper.toDto(patient);
    }

    @Transactional
    public PatientResponseDTO merge(UUID sourceId, UUID targetId) {
        if (sourceId.equals(targetId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A patient cannot be merged into itself");
        Patient source = requirePatient(sourceId);
        Patient target = requirePatient(targetId);
        ensureMutable(source);
        ensureMutable(target);
        source.setStatus(PatientStatus.MERGED);
        source.setMergedIntoPatientId(targetId);
        source.getExternalIdentifiers().forEach(target.getExternalIdentifiers()::putIfAbsent);
        mergeHistoryRepository.save(new PatientMergeHistory(sourceId, targetId));
        outbox.append("PATIENT_MERGED", source);
        return PatientMapper.toDto(source);
    }

    @Transactional
    public PatientResponseDTO unmerge(UUID sourceId) {
        Patient source = requirePatient(sourceId);
        if (source.getStatus() != PatientStatus.MERGED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient is not merged");
        }
        PatientMergeHistory history = mergeHistoryRepository
                .findFirstBySourcePatientIdAndUnmergedAtIsNullOrderByMergedAtDesc(sourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Active merge history was not found"));
        history.markUnmerged();
        source.setMergedIntoPatientId(null);
        source.setStatus(PatientStatus.ACTIVE);
        outbox.append("PATIENT_UNMERGED", source);
        return PatientMapper.toDto(source);
    }

    @Transactional
    public void deletePatient(UUID id) {
        Patient patient = requirePatient(id);
        patient.setStatus(PatientStatus.ARCHIVED);
        outbox.append("PATIENT_ARCHIVED", patient);
    }

    private Patient requirePatient(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with the ID: " + id));
    }

    private void ensureMutable(Patient patient) {
        if (patient.getStatus() == PatientStatus.MERGED || patient.getStatus() == PatientStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient lifecycle state does not allow this operation");
        }
    }

    private String generateMrn() { return "MRN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(); }
}
