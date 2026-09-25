package git.jogindermikael.appointmentservice.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record AppointmentResourceRequest(@NotBlank String resourceType, @NotBlank String name,
        UUID locationId) {
}
