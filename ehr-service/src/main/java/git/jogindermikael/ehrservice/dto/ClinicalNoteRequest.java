package git.jogindermikael.ehrservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ClinicalNoteRequest(@NotNull UUID encounterId, @NotNull UUID patientId,
        @NotNull UUID clinicianId, @NotBlank String body) {
}
