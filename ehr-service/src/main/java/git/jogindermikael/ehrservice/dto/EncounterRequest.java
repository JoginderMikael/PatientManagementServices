package git.jogindermikael.ehrservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record EncounterRequest(@NotNull UUID patientId, @NotNull UUID clinicianId,
        Instant startedAt, @NotBlank String reason) {
}
