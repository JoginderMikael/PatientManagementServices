package git.jogindermikael.ehrservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record ClinicalAmendmentRequest(@NotBlank String reason, @NotBlank String status,
        @NotNull Map<String, Object> payload) {
}
