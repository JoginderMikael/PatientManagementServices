package git.jogindermikael.analyticsservice.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record RegulatoryReportRequest(@NotBlank String reportType, LocalDate periodStart,
        LocalDate periodEnd) {
}
