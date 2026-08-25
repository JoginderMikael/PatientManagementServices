package git.jogindermikael.auditcomplianceservice.model;

import java.time.Instant;
import java.util.UUID;

public record AuditEvent(UUID id, UUID actorId, String actorRole, String action, UUID patientId, String resourceType, UUID resourceId, String sourceService, String reason, Instant occurredAt) {}
