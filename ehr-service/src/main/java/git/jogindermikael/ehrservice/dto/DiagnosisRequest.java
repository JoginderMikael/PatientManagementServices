package git.jogindermikael.ehrservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record DiagnosisRequest(@NotNull UUID patientId, @NotNull UUID clinicianId,
        @NotBlank String code, @NotBlank String description, @NotNull LocalDate diagnosedOn,
        UUID encounterId) {
}
