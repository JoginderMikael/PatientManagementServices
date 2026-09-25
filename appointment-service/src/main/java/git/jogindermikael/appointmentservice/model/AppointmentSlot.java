package git.jogindermikael.appointmentservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment_slot")
public class AppointmentSlot {
  @Id private UUID id;
  @Column(nullable = false) private UUID appointmentId;
  @Column(nullable = false) private UUID doctorId;
  @Column(nullable = false) private LocalDateTime slotTime;

  protected AppointmentSlot() {}

  public AppointmentSlot(UUID appointmentId, UUID doctorId, LocalDateTime slotTime) {
    id = UUID.randomUUID();
    this.appointmentId = appointmentId;
    this.doctorId = doctorId;
    this.slotTime = slotTime;
  }

  public UUID getId() { return id; }
  public UUID getAppointmentId() { return appointmentId; }
  public UUID getDoctorId() { return doctorId; }
  public LocalDateTime getSlotTime() { return slotTime; }
}
