package git.jogindermikael.ehrservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lab_result")
public class LabResult {
    @Id private UUID id;
    @Column(nullable = false) private UUID patientId;
    private UUID encounterId;
    @Column(nullable = false) private String testName;
    @Column(nullable = false, columnDefinition = "TEXT") private String resultSummary;
    @Column(nullable = false) private String source;
    @Column(nullable = false) private LocalDate collectedOn;
    @Column(nullable = false) private Instant importedAt;

    protected LabResult() {}

    public LabResult(UUID id, UUID patientId, UUID encounterId, String testName,
            String resultSummary, String source, LocalDate collectedOn, Instant importedAt) {
        this.id = id; this.patientId = patientId; this.encounterId = encounterId;
        this.testName = testName; this.resultSummary = resultSummary; this.source = source;
        this.collectedOn = collectedOn; this.importedAt = importedAt;
    }

    public UUID id() { return id; }
    public UUID getId() { return id; }
    public UUID patientId() { return patientId; }
    public UUID getPatientId() { return patientId; }
    public UUID getEncounterId() { return encounterId; }
    public String testName() { return testName; }
    public String getTestName() { return testName; }
    public String resultSummary() { return resultSummary; }
    public String getResultSummary() { return resultSummary; }
    public String source() { return source; }
    public String getSource() { return source; }
    public LocalDate collectedOn() { return collectedOn; }
    public LocalDate getCollectedOn() { return collectedOn; }
    public Instant importedAt() { return importedAt; }
    public Instant getImportedAt() { return importedAt; }
}
