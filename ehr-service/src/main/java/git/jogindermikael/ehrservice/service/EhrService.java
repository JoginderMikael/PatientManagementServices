package git.jogindermikael.ehrservice.service;

import git.jogindermikael.ehrservice.dto.*;
import git.jogindermikael.ehrservice.mapper.EhrMapper;
import git.jogindermikael.ehrservice.model.*;
import git.jogindermikael.ehrservice.repository.ClinicalNoteRepository;
import git.jogindermikael.ehrservice.repository.EhrRepository;
import git.jogindermikael.ehrservice.repository.EncounterRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class EhrService {
  private final EhrRepository repository;
  private final EhrMapper mapper;
  private final EncounterRepository encounters;
  private final ClinicalNoteRepository notes;
  private final ClinicalSafetyService safety;

  public EhrService(
      EhrRepository repository,
      EhrMapper mapper,
      EncounterRepository encounters,
      ClinicalNoteRepository notes,
      ClinicalSafetyService safety) {
    this.safety = safety;
    this.repository = repository;
    this.mapper = mapper;
    this.encounters = encounters;
    this.notes = notes;
  }

  public Encounter startEncounter(EncounterRequest request) {
    return encounters.save(mapper.toEncounter(request));
  }

  @Transactional(readOnly = true)
  public List<Encounter> encountersForPatient(UUID patientId) {
    return encounters.findByPatientIdOrderByStartedAtDesc(patientId);
  }

  public Encounter closeEncounter(UUID id) {
    Encounter encounter = requireEncounter(id);
    if (!"FINISHED".equals(encounter.status())) encounter.close();
    return encounter;
  }

  public MedicalHistory recordHistory(MedicalHistoryRequest request) {
    safety.lock();
    return repository.saveHistory(mapper.toHistory(request));
  }

  @Transactional(readOnly = true)
  public List<MedicalHistory> historiesForPatient(UUID patientId) {
    return repository.findHistories().stream()
        .filter(item -> item.patientId().equals(patientId))
        .toList();
  }

  public Diagnosis recordDiagnosis(DiagnosisRequest request) {
    validateEncounter(request.encounterId(), request.patientId());
    return repository.saveDiagnosis(mapper.toDiagnosis(request));
  }

  @Transactional(readOnly = true)
  public List<Diagnosis> diagnosesForPatient(UUID patientId) {
    return repository.findDiagnoses().stream()
        .filter(item -> item.patientId().equals(patientId))
        .toList();
  }

  public Prescription prescribe(PrescriptionRequest request) {
    safety.check(request.patientId(), request.medication());
    validateEncounter(request.encounterId(), request.patientId());
    return repository.savePrescription(mapper.toPrescription(request));
  }

  @Transactional(readOnly = true)
  public List<Prescription> prescriptionsForPatient(UUID patientId) {
    return repository.findPrescriptions().stream()
        .filter(item -> item.patientId().equals(patientId))
        .toList();
  }

  public LabResult addLabResult(LabResultRequest request) {
    validateEncounter(request.encounterId(), request.patientId());
    return repository.saveLabResult(mapper.toLabResult(request));
  }

  public LabResult importExternalLabResult(ExternalLabResultRequest request) {
    validateEncounter(request.encounterId(), request.patientId());
    return repository.saveLabResult(mapper.toExternalLabResult(request));
  }

  @Transactional(readOnly = true)
  public List<LabResult> labResultsForPatient(UUID patientId) {
    return repository.findLabResults().stream()
        .filter(item -> item.patientId().equals(patientId))
        .toList();
  }

  public ClinicalNote addNote(ClinicalNoteRequest request) {
    validateEncounter(request.encounterId(), request.patientId());
    return notes.save(mapper.toClinicalNote(request));
  }

  @Transactional(readOnly = true)
  public List<ClinicalNote> notesForPatient(UUID patientId) {
    return notes.findByPatientIdOrderByCreatedAtDesc(patientId);
  }

  public ClinicalNote signNote(UUID id) {
    ClinicalNote note =
        notes
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinical note not found"));
    if (!"SIGNED".equals(note.getStatus())) note.sign();
    return note;
  }

  public VaccinationRecord recordVaccination(VaccinationRequest request) {
    return repository.saveVaccination(mapper.toVaccination(request));
  }

  @Transactional(readOnly = true)
  public List<VaccinationRecord> vaccinationsForPatient(UUID patientId) {
    return repository.findVaccinations().stream()
        .filter(item -> item.patientId().equals(patientId))
        .toList();
  }

  private Encounter requireEncounter(UUID id) {
    return encounters
        .findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Encounter not found"));
  }

  private void validateEncounter(UUID encounterId, UUID patientId) {
    if (encounterId == null) return;
    Encounter encounter = requireEncounter(encounterId);
    if ("FINISHED".equals(encounter.status()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Encounter is closed");
    if (!encounter.patientId().equals(patientId))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Encounter belongs to another patient");
  }
}
