package git.jogindermikael.auditcomplianceservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Hold(boolean enabled, @NotBlank @Size(max = 200) String evidenceReference) {
}
