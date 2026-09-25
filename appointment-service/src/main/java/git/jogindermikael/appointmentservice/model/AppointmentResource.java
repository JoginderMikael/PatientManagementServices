package git.jogindermikael.appointmentservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.UUID;

@Entity
@Table(name = "appointment_resource")
public class AppointmentResource {
  @Id
  private UUID id;
  @Column(nullable = false)
  private String resourceType;
  @Column(nullable = false)
  private String name;
  private UUID locationId;
  @Column(nullable = false)
  private boolean active = true;
  @Version
  private long version;

  protected AppointmentResource() {
  }

  public AppointmentResource(UUID id, String resourceType, String name, UUID locationId) {
    this.id = id;
    this.resourceType = resourceType;
    this.name = name;
    this.locationId = locationId;
  }

  public UUID getId() {
    return id;
  }

  public String getResourceType() {
    return resourceType;
  }

  public String getName() {
    return name;
  }

  public UUID getLocationId() {
    return locationId;
  }

  public boolean isActive() {
    return active;
  }

  public long getVersion() {
    return version;
  }
}
