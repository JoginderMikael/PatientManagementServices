package git.jogindermikael.appointmentservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "doctor_schedule")
public class DoctorSchedule {
  @Id
  private UUID id;
  @Column(nullable = false)
  private UUID doctorId;
  @Column(nullable = false)
  private LocalDate workDate;
  @Column(nullable = false)
  private LocalTime startsAt;
  @Column(nullable = false)
  private LocalTime endsAt;
  @Column(nullable = false)
  private String location;
  @Version
  private long version;

  protected DoctorSchedule() {
  }

  public DoctorSchedule(
      UUID id,
      UUID doctorId,
      LocalDate workDate,
      LocalTime startsAt,
      LocalTime endsAt,
      String location) {
    this.id = id;
    this.doctorId = doctorId;
    this.workDate = workDate;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
    this.location = location;
  }

  public UUID id() {
    return id;
  }

  public UUID getId() {
    return id;
  }

  public UUID doctorId() {
    return doctorId;
  }

  public UUID getDoctorId() {
    return doctorId;
  }

  public LocalDate workDate() {
    return workDate;
  }

  public LocalDate getWorkDate() {
    return workDate;
  }

  public LocalTime startsAt() {
    return startsAt;
  }

  public LocalTime getStartsAt() {
    return startsAt;
  }

  public LocalTime endsAt() {
    return endsAt;
  }

  public LocalTime getEndsAt() {
    return endsAt;
  }

  public String location() {
    return location;
  }

  public String getLocation() {
    return location;
  }

  public long getVersion() {
    return version;
  }
}
