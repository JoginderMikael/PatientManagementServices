package git.jogindermikael.notificationservice.model;

import java.time.Instant;
import java.util.UUID;

public record NotificationMessage(UUID id, UUID recipientId, String channel, String destination, String template, String body, String status, UUID correlationId, Instant createdAt) {}
