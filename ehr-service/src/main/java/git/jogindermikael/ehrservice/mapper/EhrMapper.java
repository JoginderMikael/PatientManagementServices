package git.jogindermikael.ehrservice.mapper;

import git.jogindermikael.ehrservice.dto.*;
import git.jogindermikael.ehrservice.model.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class EhrMapper {
    public Encounter toEncounter(EncounterRequest request) {
        Instant startedAt = request.startedAt() == null ? Instant.now() : request.startedAt();
        return new Encounter(UUID.randomUUID(), request.patientId(), request.clinicianId(), startedAt, "IN_PROGRESS", request.reason(), Instant.now());
    }
    public MedicalHistory toHistory(MedicalHistoryRequest request) {
        return new MedicalHistory(UUID.randomUUID(), request.patientId(), request.summary(), request.allergies(), request.chronicConditions(), Instant.now());
    }

    public Diagnosis toDiagnosis(DiagnosisRequest request) {
        return new Diagnosis(UUID.randomUUID(), request.patientId(), request.encounterId(), request.clinicianId(), request.code(), request.description(), request.diagnosedOn(), Instant.now());
    }

    public Prescription toPrescription(PrescriptionRequest request) {
        return new Prescription(UUID.randomUUID(), request.patientId(), request.encounterId(), request.clinicianId(), request.medication(), request.dosage(), request.instructions(), "ACTIVE", Instant.now());
    }

    public LabResult toLabResult(LabResultRequest request) {
        return new LabResult(UUID.randomUUID(), request.patientId(), request.encounterId(), request.testName(), request.resultSummary(), request.source(), request.collectedOn(), Instant.now());
    }

    public LabResult toExternalLabResult(ExternalLabResultRequest request) {
        return new LabResult(UUID.randomUUID(), request.patientId(), request.encounterId(), request.testName(), request.resultSummary(), "LIS:" + request.externalSystem(), request.collectedOn(), Instant.now());
    }

    public VaccinationRecord toVaccination(VaccinationRequest request) {
        return new VaccinationRecord(UUID.randomUUID(), request.patientId(), request.vaccine(), request.administeredOn(), request.lotNumber(), Instant.now());
    }

    public ClinicalNote toClinicalNote(ClinicalNoteRequest request) {
        return new ClinicalNote(UUID.randomUUID(), request.encounterId(), request.patientId(), request.clinicianId(), request.body());
    }
}
