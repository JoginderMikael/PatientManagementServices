package git.jogindermikael.ehrservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record ExternalLabResultRequest(@NotNull UUID patientId, @NotBlank String externalSystem,
        @NotBlank String testName, @NotBlank String resultSummary, @NotNull LocalDate collectedOn,
        UUID encounterId) {
}
