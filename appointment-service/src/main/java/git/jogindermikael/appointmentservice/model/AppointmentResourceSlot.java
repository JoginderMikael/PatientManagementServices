package git.jogindermikael.appointmentservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment_resource_slot")
public class AppointmentResourceSlot {
  @Id private UUID id;
  @Column(nullable = false) private UUID appointmentId;
  @Column(nullable = false) private UUID resourceId;
  @Column(nullable = false) private LocalDateTime slotTime;

  protected AppointmentResourceSlot() {}

  public AppointmentResourceSlot(UUID appointmentId, UUID resourceId, LocalDateTime slotTime) {
    id = UUID.randomUUID();
    this.appointmentId = appointmentId;
    this.resourceId = resourceId;
    this.slotTime = slotTime;
  }

  public UUID getId() { return id; }
  public UUID getAppointmentId() { return appointmentId; }
  public UUID getResourceId() { return resourceId; }
  public LocalDateTime getSlotTime() { return slotTime; }
}
