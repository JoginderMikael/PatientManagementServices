package git.jogindermikael.auditcomplianceservice.dto;

import git.jogindermikael.auditcomplianceservice.model.Kind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record Create(@NotNull Kind kind, @NotBlank @Size(max = 200) String owner,
        @NotBlank @Size(max = 200) String evidenceReference, @NotNull Instant dueAt) {
}
