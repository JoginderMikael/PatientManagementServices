package git.jogindermikael.analyticsservice.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class PopulationHealthModels {
    private PopulationHealthModels() {
    }

    public record HealthTrend(UUID id, String metric, String segment, LocalDate periodStart, LocalDate periodEnd, int value, Instant createdAt) {}
    public record ChronicDiseaseCohort(UUID id, String condition, String riskLevel, int patientCount, Instant createdAt) {}
    public record RegulatoryReport(UUID id, String reportType, LocalDate periodStart, LocalDate periodEnd, String status, Instant createdAt) {}
}
