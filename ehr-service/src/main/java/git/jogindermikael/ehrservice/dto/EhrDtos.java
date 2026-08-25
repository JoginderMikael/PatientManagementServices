package git.jogindermikael.ehrservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class EhrDtos {
    private EhrDtos() {
    }

    public record MedicalHistoryRequest(@NotNull UUID patientId, @NotBlank String summary, List<String> allergies, List<String> chronicConditions) {}
    public record DiagnosisRequest(@NotNull UUID patientId, @NotNull UUID clinicianId, @NotBlank String code, @NotBlank String description, @NotNull LocalDate diagnosedOn) {}
    public record PrescriptionRequest(@NotNull UUID patientId, @NotNull UUID clinicianId, @NotBlank String medication, @NotBlank String dosage, @NotBlank String instructions) {}
    public record LabResultRequest(@NotNull UUID patientId, @NotBlank String testName, @NotBlank String resultSummary, @NotBlank String source, @NotNull LocalDate collectedOn) {}
    public record ExternalLabResultRequest(@NotNull UUID patientId, @NotBlank String externalSystem, @NotBlank String testName, @NotBlank String resultSummary, @NotNull LocalDate collectedOn) {}
    public record VaccinationRequest(@NotNull UUID patientId, @NotBlank String vaccine, @NotNull LocalDate administeredOn, @NotBlank String lotNumber) {}
}
