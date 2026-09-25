package git.jogindermikael.patientportalservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RecordAccessCommand(@NotNull UUID patientId, @NotBlank String recordType) {
}
