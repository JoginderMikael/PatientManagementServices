package git.jogindermikael.appointmentservice.dto;

import jakarta.validation.constraints.NotBlank;

public record VirtualConsultationRequest(@NotBlank String provider, @NotBlank String joinUrl) {
}
