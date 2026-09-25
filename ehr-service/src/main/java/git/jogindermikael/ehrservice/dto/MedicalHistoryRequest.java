package git.jogindermikael.ehrservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record MedicalHistoryRequest(@NotNull UUID patientId, @NotBlank String summary,
        List<String> allergies, List<String> chronicConditions) {
}
