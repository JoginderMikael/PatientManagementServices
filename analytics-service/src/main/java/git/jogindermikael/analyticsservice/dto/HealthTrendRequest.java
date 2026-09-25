package git.jogindermikael.analyticsservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record HealthTrendRequest(@NotBlank String metric, @NotBlank String segment,
        LocalDate periodStart, LocalDate periodEnd, @Min(0) int value) {
}
