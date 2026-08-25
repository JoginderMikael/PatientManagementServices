package git.jogindermikael.ehrservice.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class EhrModels {
    private EhrModels() {
    }

    public record MedicalHistory(UUID id, UUID patientId, String summary, List<String> allergies, List<String> chronicConditions, Instant createdAt) {}
    public record Diagnosis(UUID id, UUID patientId, UUID clinicianId, String code, String description, LocalDate diagnosedOn, Instant createdAt) {}
    public record Prescription(UUID id, UUID patientId, UUID clinicianId, String medication, String dosage, String instructions, String status, Instant createdAt) {}
    public record LabResult(UUID id, UUID patientId, String testName, String resultSummary, String source, LocalDate collectedOn, Instant importedAt) {}
    public record VaccinationRecord(UUID id, UUID patientId, String vaccine, LocalDate administeredOn, String lotNumber, Instant createdAt) {}
}
