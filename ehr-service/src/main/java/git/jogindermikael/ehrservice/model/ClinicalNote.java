package git.jogindermikael.ehrservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clinical_note")
public class ClinicalNote {
    @Id private UUID id;
    @Column(nullable = false) private UUID encounterId;
    @Column(nullable = false) private UUID patientId;
    @Column(nullable = false) private UUID clinicianId;
    @Column(nullable = false, columnDefinition = "TEXT") private String body;
    @Column(nullable = false) private String status;
    private Instant signedAt;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;
    @Version private long version;

    protected ClinicalNote() {}

    public ClinicalNote(UUID id, UUID encounterId, UUID patientId, UUID clinicianId, String body) {
        this.id = id; this.encounterId = encounterId; this.patientId = patientId;
        this.clinicianId = clinicianId; this.body = body; this.status = "DRAFT";
        this.createdAt = Instant.now(); this.updatedAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getEncounterId() { return encounterId; }
    public UUID getPatientId() { return patientId; }
    public UUID getClinicianId() { return clinicianId; }
    public String getBody() { return body; }
    public String getStatus() { return status; }
    public Instant getSignedAt() { return signedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
    public void sign() { status = "SIGNED"; signedAt = Instant.now(); updatedAt = signedAt; }
}
