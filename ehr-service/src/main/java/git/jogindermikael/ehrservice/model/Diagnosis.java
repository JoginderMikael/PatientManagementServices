package git.jogindermikael.ehrservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "diagnosis")
public class Diagnosis {
    @Id private UUID id;
    @Column(nullable = false) private UUID patientId;
    private UUID encounterId;
    @Column(nullable = false) private UUID clinicianId;
    @Column(nullable = false) private String code;
    @Column(nullable = false) private String description;
    @Column(nullable = false) private LocalDate diagnosedOn;
    @Column(nullable = false) private Instant createdAt;

    protected Diagnosis() {}

    public Diagnosis(UUID id, UUID patientId, UUID encounterId, UUID clinicianId, String code,
            String description, LocalDate diagnosedOn, Instant createdAt) {
        this.id = id; this.patientId = patientId; this.encounterId = encounterId;
        this.clinicianId = clinicianId; this.code = code; this.description = description;
        this.diagnosedOn = diagnosedOn; this.createdAt = createdAt;
    }

    public UUID id() { return id; }
    public UUID getId() { return id; }
    public UUID patientId() { return patientId; }
    public UUID getPatientId() { return patientId; }
    public UUID getEncounterId() { return encounterId; }
    public UUID clinicianId() { return clinicianId; }
    public UUID getClinicianId() { return clinicianId; }
    public String code() { return code; }
    public String getCode() { return code; }
    public String description() { return description; }
    public String getDescription() { return description; }
    public LocalDate diagnosedOn() { return diagnosedOn; }
    public LocalDate getDiagnosedOn() { return diagnosedOn; }
    public Instant createdAt() { return createdAt; }
    public Instant getCreatedAt() { return createdAt; }
}
