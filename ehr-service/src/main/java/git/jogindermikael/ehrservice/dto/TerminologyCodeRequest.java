package git.jogindermikael.ehrservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record TerminologyCodeRequest(@NotBlank String systemUri, @NotBlank String code,
        @NotBlank String display, @NotNull Set<String> resourceTypes) {
}
