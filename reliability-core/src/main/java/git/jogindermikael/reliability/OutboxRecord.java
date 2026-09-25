package git.jogindermikael.reliability;

import java.time.Instant;
import java.util.UUID;

public record OutboxRecord(
    UUID id,
    String topic,
    String eventKey,
    String payload,
    String status,
    int attempts,
    int replayCount,
    Instant availableAt,
    Instant createdAt,
    Instant publishedAt,
    Instant deadLetteredAt,
    String lastError) {}
