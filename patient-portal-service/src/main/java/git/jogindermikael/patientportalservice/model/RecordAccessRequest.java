package git.jogindermikael.patientportalservice.model;

import java.time.Instant;
import java.util.UUID;

public record RecordAccessRequest(UUID id, UUID patientId, String recordType, String status,
        Instant createdAt) {
}
