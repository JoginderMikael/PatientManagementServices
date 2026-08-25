package git.jogindermikael.analyticsservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public final class PopulationHealthDtos {
    private PopulationHealthDtos() {
    }

    public record HealthTrendRequest(@NotBlank String metric, @NotBlank String segment, LocalDate periodStart, LocalDate periodEnd, @Min(0) int value) {}
    public record ChronicDiseaseCohortRequest(@NotBlank String condition, @NotBlank String riskLevel, @Min(0) int patientCount) {}
    public record RegulatoryReportRequest(@NotBlank String reportType, LocalDate periodStart, LocalDate periodEnd) {}
}
