package git.jogindermikael.ehrservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ClinicalResourceRequest(@NotNull UUID patientId, UUID encounterId,
        @NotBlank String resourceType, @NotBlank String status, @NotBlank String codeSystem,
        @NotBlank String code, @NotBlank String display, @NotNull Instant effectiveAt,
        @NotNull Map<String, Object> payload) {
}
