package git.jogindermikael.patientportalservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Release(@NotBlank @Size(max = 1000000) String content) {
}
