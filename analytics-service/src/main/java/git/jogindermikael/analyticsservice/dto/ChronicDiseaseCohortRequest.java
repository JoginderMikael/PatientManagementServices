package git.jogindermikael.analyticsservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ChronicDiseaseCohortRequest(@NotBlank String condition, @NotBlank String riskLevel,
        @Min(0) int patientCount) {
}
