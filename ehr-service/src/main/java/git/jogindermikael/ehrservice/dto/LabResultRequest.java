package git.jogindermikael.ehrservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record LabResultRequest(@NotNull UUID patientId, @NotBlank String testName,
        @NotBlank String resultSummary, @NotBlank String source, @NotNull LocalDate collectedOn,
        UUID encounterId) {
}
