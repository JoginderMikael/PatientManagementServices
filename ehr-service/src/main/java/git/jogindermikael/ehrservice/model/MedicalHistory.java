package git.jogindermikael.ehrservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "medical_history")
public class MedicalHistory {
    @Id private UUID id;
    @Column(nullable = false) private UUID patientId;
    @Column(nullable = false, columnDefinition = "TEXT") private String summary;
    @Convert(converter = StringListConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT") private List<String> allergies;
    @Convert(converter = StringListConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT") private List<String> chronicConditions;
    @Column(nullable = false) private Instant createdAt;

    protected MedicalHistory() {}

    public MedicalHistory(UUID id, UUID patientId, String summary, List<String> allergies,
            List<String> chronicConditions, Instant createdAt) {
        this.id = id; this.patientId = patientId; this.summary = summary; this.allergies = allergies;
        this.chronicConditions = chronicConditions; this.createdAt = createdAt;
    }

    public UUID id() { return id; }
    public UUID getId() { return id; }
    public UUID patientId() { return patientId; }
    public UUID getPatientId() { return patientId; }
    public String summary() { return summary; }
    public String getSummary() { return summary; }
    public List<String> allergies() { return allergies; }
    public List<String> getAllergies() { return allergies; }
    public List<String> chronicConditions() { return chronicConditions; }
    public List<String> getChronicConditions() { return chronicConditions; }
    public Instant createdAt() { return createdAt; }
    public Instant getCreatedAt() { return createdAt; }
}
