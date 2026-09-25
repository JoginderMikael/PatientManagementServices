package git.jogindermikael.reliability.dto;

import java.time.Instant;
import java.util.UUID;

public record InboxRecord(UUID eventId, String consumer, String eventType, String status,
        int attempts, String payloadHash, Instant receivedAt, Instant processedAt,
        String lastError) {
}
