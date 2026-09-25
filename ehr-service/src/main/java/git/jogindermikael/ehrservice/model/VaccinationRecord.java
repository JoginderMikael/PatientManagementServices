package git.jogindermikael.ehrservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vaccination_record")
public class VaccinationRecord {
    @Id private UUID id;
    @Column(nullable = false) private UUID patientId;
    @Column(nullable = false) private String vaccine;
    @Column(nullable = false) private LocalDate administeredOn;
    @Column(nullable = false) private String lotNumber;
    @Column(nullable = false) private Instant createdAt;

    protected VaccinationRecord() {}

    public VaccinationRecord(UUID id, UUID patientId, String vaccine, LocalDate administeredOn,
            String lotNumber, Instant createdAt) {
        this.id = id; this.patientId = patientId; this.vaccine = vaccine;
        this.administeredOn = administeredOn; this.lotNumber = lotNumber; this.createdAt = createdAt;
    }

    public UUID id() { return id; }
    public UUID getId() { return id; }
    public UUID patientId() { return patientId; }
    public UUID getPatientId() { return patientId; }
    public String vaccine() { return vaccine; }
    public String getVaccine() { return vaccine; }
    public LocalDate administeredOn() { return administeredOn; }
    public LocalDate getAdministeredOn() { return administeredOn; }
    public String lotNumber() { return lotNumber; }
    public String getLotNumber() { return lotNumber; }
    public Instant createdAt() { return createdAt; }
    public Instant getCreatedAt() { return createdAt; }
}
