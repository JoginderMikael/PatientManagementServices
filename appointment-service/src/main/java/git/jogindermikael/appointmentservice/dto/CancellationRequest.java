package git.jogindermikael.appointmentservice.dto;

import jakarta.validation.constraints.NotBlank;

public record CancellationRequest(@NotBlank String reason) {
}
