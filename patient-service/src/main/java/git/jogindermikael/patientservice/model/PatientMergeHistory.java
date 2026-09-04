package git.jogindermikael.patientservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "patient_merge_history")
public class PatientMergeHistory {
    @Id private UUID id;
    @Column(nullable = false) private UUID sourcePatientId;
    @Column(nullable = false) private UUID targetPatientId;
    @Column(nullable = false) private Instant mergedAt;
    private Instant unmergedAt;

    protected PatientMergeHistory() {}
    public PatientMergeHistory(UUID sourcePatientId, UUID targetPatientId) {
        id = UUID.randomUUID();
        this.sourcePatientId = sourcePatientId;
        this.targetPatientId = targetPatientId;
        mergedAt = Instant.now();
    }
    public UUID getId() { return id; }
    public UUID getSourcePatientId() { return sourcePatientId; }
    public UUID getTargetPatientId() { return targetPatientId; }
    public Instant getMergedAt() { return mergedAt; }
    public Instant getUnmergedAt() { return unmergedAt; }
    public void markUnmerged() { unmergedAt = Instant.now(); }
}
