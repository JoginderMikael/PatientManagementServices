package git.jogindermikael.staffdashboardservice.model;

import java.time.Instant;
import java.util.UUID;

public record ClinicalRound(UUID id, UUID staffId, UUID patientId, String unit, String notes,
        String status, Instant updatedAt) {
}
