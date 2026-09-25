package git.jogindermikael.ehrservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record VaccinationRequest(@NotNull UUID patientId, @NotBlank String vaccine,
        @NotNull LocalDate administeredOn, @NotBlank String lotNumber) {
}
