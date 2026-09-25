package git.jogindermikael.staffdashboardservice.model;

import java.time.Instant;
import java.util.UUID;

public record StaffTask(UUID id, UUID assigneeId, UUID patientId, String title, String priority,
        String status, Instant updatedAt) {
}
