package git.jogindermikael.auditcomplianceservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_event")
public class AuditEvent {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true)
    private UUID sourceEventId;
    @Column(nullable = false)
    private String actorId;
    @Column(nullable = false)
    private String actorRole;
    @Column(nullable = false)
    private String action;
    private UUID patientId;
    @Column(nullable = false)
    private String resourceType;
    private UUID resourceId;
    @Column(nullable = false)
    private String sourceService;
    @Column(nullable = false)
    private String outcome;
    private String reason;
    private String endpoint;
    private String requestId;
    private String correlationId;
    @Column(nullable = false)
    private Instant occurredAt;
    @Column(nullable = false, length = 64)
    private String previousHash;
    @Column(nullable = false, length = 64)
    private String eventHash;
    @Column(nullable = false)
    private short hashVersion = 2;

    public short getHashVersion() { return hashVersion; }

    protected AuditEvent() {
    }

    public AuditEvent(UUID id, UUID sourceEventId, String actorId, String actorRole, String action, UUID patientId,
            String resourceType, UUID resourceId, String sourceService, String outcome, String reason, String endpoint,
            String requestId, String correlationId, Instant occurredAt, String previousHash, String eventHash) {
        this.id = id;
        this.sourceEventId = sourceEventId;
        this.actorId = actorId;
        this.actorRole = actorRole;
        this.action = action;
        this.patientId = patientId;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.sourceService = sourceService;
        this.outcome = outcome;
        this.reason = reason;
        this.endpoint = endpoint;
        this.requestId = requestId;
        this.correlationId = correlationId;
        this.occurredAt = occurredAt;
        this.previousHash = previousHash;
        this.eventHash = eventHash;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSourceEventId() {
        return sourceEventId;
    }

    public String getActorId() {
        return actorId;
    }

    public String getActorRole() {
        return actorRole;
    }

    public String getAction() {
        return action;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public String getSourceService() {
        return sourceService;
    }

    public String getOutcome() {
        return outcome;
    }

    public String getReason() {
        return reason;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getEventHash() {
        return eventHash;
    }
}
