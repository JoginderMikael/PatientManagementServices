package git.jogindermikael.ehrservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "encounter")
public class Encounter {
    @Id private UUID id;
    @Column(nullable = false) private UUID patientId;
    @Column(nullable = false) private UUID clinicianId;
    @Column(nullable = false) private Instant startedAt;
    private Instant endedAt;
    @Column(nullable = false) private String status;
    @Column(nullable = false) private String reason;
    @Column(nullable = false) private Instant createdAt;
    @Version private long version;

    protected Encounter() {}

    public Encounter(UUID id, UUID patientId, UUID clinicianId, Instant startedAt, String status,
            String reason, Instant createdAt) {
        this.id = id; this.patientId = patientId; this.clinicianId = clinicianId;
        this.startedAt = startedAt; this.status = status; this.reason = reason; this.createdAt = createdAt;
    }

    public UUID id() { return id; }
    public UUID getId() { return id; }
    public UUID patientId() { return patientId; }
    public UUID getPatientId() { return patientId; }
    public UUID clinicianId() { return clinicianId; }
    public UUID getClinicianId() { return clinicianId; }
    public Instant startedAt() { return startedAt; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getEndedAt() { return endedAt; }
    public String status() { return status; }
    public String getStatus() { return status; }
    public String reason() { return reason; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
    public long getVersion() { return version; }
    public void close() { endedAt = Instant.now(); status = "FINISHED"; }
}
