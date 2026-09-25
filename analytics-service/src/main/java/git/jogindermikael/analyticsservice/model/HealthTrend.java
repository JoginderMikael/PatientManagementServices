package git.jogindermikael.analyticsservice.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record HealthTrend(UUID id, String metric, String segment, LocalDate periodStart,
        LocalDate periodEnd, int value, Instant createdAt) {
}
