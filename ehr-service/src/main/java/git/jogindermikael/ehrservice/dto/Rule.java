package git.jogindermikael.ehrservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Rule(@NotBlank @Size(max = 200) String medication,
        @NotBlank @Size(max = 200) String interactingMedication,
        @NotBlank @Size(max = 1000) String reason) {
}
