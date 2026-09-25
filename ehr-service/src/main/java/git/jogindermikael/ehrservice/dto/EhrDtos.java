package git.jogindermikael.ehrservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.Set;

public final class EhrDtos {
    private EhrDtos() {
    }

    public record EncounterRequest(@NotNull UUID patientId, @NotNull UUID clinicianId, Instant startedAt, @NotBlank String reason) {}
    public record MedicalHistoryRequest(@NotNull UUID patientId, @NotBlank String summary, List<String> allergies, List<String> chronicConditions) {}
    public record DiagnosisRequest(@NotNull UUID patientId, @NotNull UUID clinicianId, @NotBlank String code, @NotBlank String description, @NotNull LocalDate diagnosedOn, UUID encounterId) {}
    public record PrescriptionRequest(@NotNull UUID patientId, @NotNull UUID clinicianId, @NotBlank String medication, @NotBlank String dosage, @NotBlank String instructions, UUID encounterId) {}
    public record LabResultRequest(@NotNull UUID patientId, @NotBlank String testName, @NotBlank String resultSummary, @NotBlank String source, @NotNull LocalDate collectedOn, UUID encounterId) {}
    public record ExternalLabResultRequest(@NotNull UUID patientId, @NotBlank String externalSystem, @NotBlank String testName, @NotBlank String resultSummary, @NotNull LocalDate collectedOn, UUID encounterId) {}
    public record ClinicalNoteRequest(@NotNull UUID encounterId, @NotNull UUID patientId, @NotNull UUID clinicianId, @NotBlank String body) {}
    public record VaccinationRequest(@NotNull UUID patientId, @NotBlank String vaccine, @NotNull LocalDate administeredOn, @NotBlank String lotNumber) {}
    public record TerminologyCodeRequest(@NotBlank String systemUri, @NotBlank String code,
            @NotBlank String display, @NotNull Set<String> resourceTypes) {}
    public record ClinicalResourceRequest(@NotNull UUID patientId, UUID encounterId,
            @NotBlank String resourceType, @NotBlank String status, @NotBlank String codeSystem,
            @NotBlank String code, @NotBlank String display, @NotNull Instant effectiveAt,
            @NotNull Map<String, Object> payload) {}
    public record ClinicalAmendmentRequest(@NotBlank String reason, @NotBlank String status,
            @NotNull Map<String, Object> payload) {}
}
