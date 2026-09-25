package git.jogindermikael.appointmentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ConsentFormRequest(@NotNull UUID patientId, @NotBlank String formType,
        @NotBlank String signature) {
}
