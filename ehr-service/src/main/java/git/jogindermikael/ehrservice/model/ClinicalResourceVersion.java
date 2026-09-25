package git.jogindermikael.ehrservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clinical_resource_version")
public class ClinicalResourceVersion {
    @Id private UUID id;
    @Column(nullable = false) private UUID resourceId;
    @Column(nullable = false) private int versionNumber;
    @Column(nullable = false, columnDefinition = "TEXT") private String payload;
    private String amendmentReason;
    @Column(nullable = false) private String recordedBy;
    @Column(nullable = false) private Instant recordedAt;
    private String previousHash;
    @Column(nullable = false) private String contentHash;

    protected ClinicalResourceVersion() {}

    public ClinicalResourceVersion(UUID id, UUID resourceId, int versionNumber, String payload,
            String reason, String actor, Instant recordedAt, String previousHash, String contentHash) {
        this.id = id; this.resourceId = resourceId; this.versionNumber = versionNumber;
        this.payload = payload; this.amendmentReason = reason; this.recordedBy = actor;
        this.recordedAt = recordedAt; this.previousHash = previousHash; this.contentHash = contentHash;
    }

    public UUID getId() { return id; }
    public UUID getResourceId() { return resourceId; }
    public int getVersionNumber() { return versionNumber; }
    public String getPayload() { return payload; }
    public String getAmendmentReason() { return amendmentReason; }
    public String getRecordedBy() { return recordedBy; }
    public Instant getRecordedAt() { return recordedAt; }
    public String getPreviousHash() { return previousHash; }
    public String getContentHash() { return contentHash; }
}
