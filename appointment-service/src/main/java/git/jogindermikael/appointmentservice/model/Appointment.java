package git.jogindermikael.appointmentservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "appointment")
public class Appointment {
  @Id private UUID id;
  @Column(nullable = false) private UUID patientId;
  @Column(nullable = false) private UUID doctorId;
  @Column(nullable = false) private LocalDateTime startsAt;
  @Column(nullable = false) private LocalDateTime endsAt;
  @Column(nullable = false) private String reason;
  @Column(nullable = false, length = 24) private String status;
  private String cancellationReason;
  @Column(nullable = false) private String appointmentType = "GENERAL";
  private UUID locationId;
  private UUID roomId;
  private UUID recurrenceGroupId;
  private Instant checkedInAt;
  private Instant checkedOutAt;
  private Instant noShowAt;
  @Column(nullable = false) private Instant updatedAt;
  @Version private long version;

  protected Appointment() {}

  public Appointment(
      UUID id,
      UUID patientId,
      UUID doctorId,
      LocalDateTime startsAt,
      LocalDateTime endsAt,
      String reason,
      String status,
      Instant updatedAt) {
    this.id = id;
    this.patientId = patientId;
    this.doctorId = doctorId;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
    this.reason = reason;
    this.status = status;
    this.updatedAt = updatedAt;
  }

  public UUID id() { return id; }
  public UUID getId() { return id; }
  public UUID patientId() { return patientId; }
  public UUID getPatientId() { return patientId; }
  public UUID doctorId() { return doctorId; }
  public UUID getDoctorId() { return doctorId; }
  public LocalDateTime startsAt() { return startsAt; }
  public LocalDateTime getStartsAt() { return startsAt; }
  public LocalDateTime endsAt() { return endsAt; }
  public LocalDateTime getEndsAt() { return endsAt; }
  public String reason() { return reason; }
  public String getReason() { return reason; }
  public String status() { return status; }
  public String getStatus() { return status; }
  public String getCancellationReason() { return cancellationReason; }
  public String getAppointmentType() { return appointmentType; }
  public UUID getLocationId() { return locationId; }
  public UUID getRoomId() { return roomId; }
  public UUID getRecurrenceGroupId() { return recurrenceGroupId; }
  public Instant getCheckedInAt() { return checkedInAt; }
  public Instant getCheckedOutAt() { return checkedOutAt; }
  public Instant getNoShowAt() { return noShowAt; }
  public Instant updatedAt() { return updatedAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public long getVersion() { return version; }

  public void cancel(String reason) {
    status = "CANCELLED";
    cancellationReason = reason;
    updatedAt = Instant.now();
  }

  public void reschedule(LocalDateTime start, int durationMinutes) {
    if (!Set.of("BOOKED", "RESCHEDULED").contains(status)) {
      throw new IllegalStateException("Appointment cannot be rescheduled");
    }
    startsAt = start;
    endsAt = start.plusMinutes(durationMinutes);
    status = "RESCHEDULED";
    updatedAt = Instant.now();
  }

  public void checkIn() {
    if (!Set.of("BOOKED", "RESCHEDULED").contains(status)) {
      throw new IllegalStateException("Appointment cannot be checked in");
    }
    status = "CHECKED_IN";
    checkedInAt = Instant.now();
    updatedAt = checkedInAt;
  }

  public void checkOut() {
    if (!"CHECKED_IN".equals(status)) {
      throw new IllegalStateException("Appointment must be checked in first");
    }
    status = "COMPLETED";
    checkedOutAt = Instant.now();
    updatedAt = checkedOutAt;
  }

  public void markNoShow() {
    if (!Set.of("BOOKED", "RESCHEDULED").contains(status)) {
      throw new IllegalStateException("Appointment cannot be marked no-show");
    }
    status = "NO_SHOW";
    noShowAt = Instant.now();
    updatedAt = noShowAt;
  }

  public void assignOperationalDetails(
      String type, UUID location, UUID room, UUID recurrence) {
    appointmentType = type;
    locationId = location;
    roomId = room;
    recurrenceGroupId = recurrence;
    updatedAt = Instant.now();
  }
}
