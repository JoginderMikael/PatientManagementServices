package git.jogindermikael.auditcomplianceservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Review(@NotBlank @Size(max = 200) String evidenceReference) {
}
