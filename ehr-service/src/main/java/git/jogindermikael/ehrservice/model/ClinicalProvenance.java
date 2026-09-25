package git.jogindermikael.ehrservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clinical_provenance")
public class ClinicalProvenance {
    @Id private UUID id;
    @Column(nullable = false) private UUID resourceId;
    @Column(nullable = false) private int versionNumber;
    @Column(nullable = false) private String action;
    @Column(nullable = false) private String actorId;
    @Column(nullable = false) private Instant occurredAt;
    @Column(nullable = false) private String contentHash;

    protected ClinicalProvenance() {}

    public ClinicalProvenance(UUID id, UUID resourceId, int version, String action, String actor,
            Instant at, String hash) {
        this.id = id; this.resourceId = resourceId; this.versionNumber = version;
        this.action = action; this.actorId = actor; this.occurredAt = at; this.contentHash = hash;
    }

    public UUID getId() { return id; }
    public UUID getResourceId() { return resourceId; }
    public int getVersionNumber() { return versionNumber; }
    public String getAction() { return action; }
    public String getActorId() { return actorId; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getContentHash() { return contentHash; }
}
