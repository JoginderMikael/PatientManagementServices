package git.jogindermikael.ehrservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record Alert(@NotNull UUID patientId, @NotNull UUID assigneeId,
        @NotBlank @Size(max = 200) String sourceReference,
        @NotBlank @Size(max = 2000) String summary) {
}
