package git.jogindermikael.staffdashboardservice.model;

import java.time.Instant;
import java.util.UUID;

public record PatientChartSummary(UUID id, UUID patientId, String summary, String riskLevel,
        Instant updatedAt) {
}
