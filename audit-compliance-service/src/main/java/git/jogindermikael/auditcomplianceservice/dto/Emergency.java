package git.jogindermikael.auditcomplianceservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record Emergency(@NotNull UUID patientId, @NotNull Instant expiresAt,
        @NotBlank @Size(max = 200) String evidenceReference) {
}
