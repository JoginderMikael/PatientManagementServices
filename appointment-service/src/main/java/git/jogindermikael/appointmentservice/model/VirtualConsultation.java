package git.jogindermikael.appointmentservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "virtual_consultation")
public class VirtualConsultation {
  @Id private UUID id;
  @Column(nullable = false) private UUID appointmentId;
  @Column(nullable = false) private String provider;
  @Column(nullable = false) private String joinUrl;
  @Column(nullable = false) private String status;
  @Column(nullable = false) private Instant createdAt;

  protected VirtualConsultation() {}

  public VirtualConsultation(
      UUID id,
      UUID appointmentId,
      String provider,
      String joinUrl,
      String status,
      Instant createdAt) {
    this.id = id;
    this.appointmentId = appointmentId;
    this.provider = provider;
    this.joinUrl = joinUrl;
    this.status = status;
    this.createdAt = createdAt;
  }

  public UUID id() { return id; }
  public UUID getId() { return id; }
  public UUID appointmentId() { return appointmentId; }
  public UUID getAppointmentId() { return appointmentId; }
  public String provider() { return provider; }
  public String getProvider() { return provider; }
  public String joinUrl() { return joinUrl; }
  public String getJoinUrl() { return joinUrl; }
  public String status() { return status; }
  public String getStatus() { return status; }
  public Instant createdAt() { return createdAt; }
  public Instant getCreatedAt() { return createdAt; }
}
