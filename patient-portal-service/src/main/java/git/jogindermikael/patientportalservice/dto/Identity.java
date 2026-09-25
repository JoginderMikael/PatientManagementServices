package git.jogindermikael.patientportalservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record Identity(@NotBlank @Size(max = 200) String subject, @NotNull UUID patientId) {
}
