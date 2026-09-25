package git.jogindermikael.appointmentservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "appointment_waitlist")
public class WaitlistEntry {
  @Id
  private UUID id;
  @Column(nullable = false)
  private UUID patientId;
  @Column(nullable = false)
  private UUID doctorId;
  @Column(nullable = false)
  private LocalDate preferredDate;
  @Column(nullable = false)
  private String reason;
  @Column(nullable = false)
  private Instant createdAt;
  @Column(nullable = false)
  private String status = "WAITING";
  private Instant offeredAt;
  private UUID promotedAppointmentId;
  @Version
  private long version;

  protected WaitlistEntry() {
  }

  public WaitlistEntry(
      UUID id,
      UUID patientId,
      UUID doctorId,
      LocalDate preferredDate,
      String reason,
      Instant createdAt) {
    this.id = id;
    this.patientId = patientId;
    this.doctorId = doctorId;
    this.preferredDate = preferredDate;
    this.reason = reason;
    this.createdAt = createdAt;
  }

  public UUID id() {
    return id;
  }

  public UUID getId() {
    return id;
  }

  public UUID patientId() {
    return patientId;
  }

  public UUID getPatientId() {
    return patientId;
  }

  public UUID doctorId() {
    return doctorId;
  }

  public UUID getDoctorId() {
    return doctorId;
  }

  public LocalDate preferredDate() {
    return preferredDate;
  }

  public LocalDate getPreferredDate() {
    return preferredDate;
  }

  public String reason() {
    return reason;
  }

  public String getReason() {
    return reason;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public String getStatus() {
    return status;
  }

  public Instant getOfferedAt() {
    return offeredAt;
  }

  public UUID getPromotedAppointmentId() {
    return promotedAppointmentId;
  }

  public long getVersion() {
    return version;
  }

  public void promote(UUID appointmentId) {
    if (!"WAITING".equals(status)) {
      throw new IllegalStateException("Waitlist entry is not waiting");
    }
    status = "PROMOTED";
    offeredAt = Instant.now();
    promotedAppointmentId = appointmentId;
  }
}
