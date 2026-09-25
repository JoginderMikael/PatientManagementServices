package git.jogindermikael.ehrservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prescription")
public class Prescription {
    @Id private UUID id;
    @Column(nullable = false) private UUID patientId;
    private UUID encounterId;
    @Column(nullable = false) private UUID clinicianId;
    @Column(nullable = false) private String medication;
    @Column(nullable = false) private String dosage;
    @Column(nullable = false, columnDefinition = "TEXT") private String instructions;
    @Column(nullable = false) private String status;
    @Column(nullable = false) private Instant createdAt;

    protected Prescription() {}

    public Prescription(UUID id, UUID patientId, UUID encounterId, UUID clinicianId,
            String medication, String dosage, String instructions, String status, Instant createdAt) {
        this.id = id; this.patientId = patientId; this.encounterId = encounterId;
        this.clinicianId = clinicianId; this.medication = medication; this.dosage = dosage;
        this.instructions = instructions; this.status = status; this.createdAt = createdAt;
    }

    public UUID id() { return id; }
    public UUID getId() { return id; }
    public UUID patientId() { return patientId; }
    public UUID getPatientId() { return patientId; }
    public UUID getEncounterId() { return encounterId; }
    public UUID clinicianId() { return clinicianId; }
    public UUID getClinicianId() { return clinicianId; }
    public String medication() { return medication; }
    public String getMedication() { return medication; }
    public String dosage() { return dosage; }
    public String getDosage() { return dosage; }
    public String instructions() { return instructions; }
    public String getInstructions() { return instructions; }
    public String status() { return status; }
    public String getStatus() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant getCreatedAt() { return createdAt; }
}
