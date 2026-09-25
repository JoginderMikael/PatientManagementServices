package git.jogindermikael.analyticsservice.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RegulatoryReport(UUID id, String reportType, LocalDate periodStart, LocalDate periodEnd,
        String status, Instant createdAt) {
}
