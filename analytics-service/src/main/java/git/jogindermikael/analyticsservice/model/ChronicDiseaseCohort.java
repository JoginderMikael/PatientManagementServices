package git.jogindermikael.analyticsservice.model;

import java.time.Instant;
import java.util.UUID;

public record ChronicDiseaseCohort(UUID id, String condition, String riskLevel, int patientCount,
        Instant createdAt) {
}
