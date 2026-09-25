package git.jogindermikael.patientportalservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PortalAppointmentCommand(@NotNull UUID patientId,
        @NotBlank String preferredSpecialty, @NotBlank String reason) {
}
