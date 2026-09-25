package git.jogindermikael.ehrservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clinical_resource")
public class ClinicalResource {
    @Id private UUID id;
    @Column(nullable = false) private UUID patientId;
    private UUID encounterId;
    @Column(nullable = false) private String resourceType;
    @Column(nullable = false) private String status;
    @Column(nullable = false) private String codeSystem;
    @Column(nullable = false) private String code;
    @Column(nullable = false) private String display;
    @Column(nullable = false) private Instant effectiveAt;
    @Column(nullable = false) private int currentVersion;
    @Column(nullable = false) private String createdBy;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;
    @Version private long lockVersion;

    protected ClinicalResource() {}

    public ClinicalResource(UUID id, UUID patientId, UUID encounterId, String type, String status,
            String system, String code, String display, Instant effectiveAt, String actor) {
        this.id = id; this.patientId = patientId; this.encounterId = encounterId;
        this.resourceType = type; this.status = status; this.codeSystem = system; this.code = code;
        this.display = display; this.effectiveAt = effectiveAt; this.currentVersion = 1;
        this.createdBy = actor; this.createdAt = Instant.now(); this.updatedAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getPatientId() { return patientId; }
    public UUID getEncounterId() { return encounterId; }
    public String getResourceType() { return resourceType; }
    public String getStatus() { return status; }
    public String getCodeSystem() { return codeSystem; }
    public String getCode() { return code; }
    public String getDisplay() { return display; }
    public Instant getEffectiveAt() { return effectiveAt; }
    public int getCurrentVersion() { return currentVersion; }
    public String getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getLockVersion() { return lockVersion; }
    public int amend(String nextStatus) {
        currentVersion++; status = nextStatus; updatedAt = Instant.now(); return currentVersion;
    }
}
