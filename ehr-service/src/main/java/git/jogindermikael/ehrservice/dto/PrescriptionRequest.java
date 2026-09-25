package git.jogindermikael.ehrservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PrescriptionRequest(@NotNull UUID patientId, @NotNull UUID clinicianId,
        @NotBlank String medication, @NotBlank String dosage, @NotBlank String instructions,
        UUID encounterId) {
}
