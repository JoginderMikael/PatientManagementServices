package git.jogindermikael.auditcomplianceservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Transition(@NotBlank @Size(max = 30) String expectedStatus,
        @NotBlank @Size(max = 30) String status,
        @NotBlank @Size(max = 200) String evidenceReference) {
}
