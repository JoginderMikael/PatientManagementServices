package git.jogindermikael.patientportalservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Settlement(@NotBlank @Size(max = 180) String providerReference) {
}
