package git.jogindermikael.appointmentservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "appointment_consent")
public class ConsentForm {
  @Id private UUID id;
  @Column(nullable = false) private UUID appointmentId;
  @Column(nullable = false) private UUID patientId;
  @Column(nullable = false) private String formType;
  @Column(nullable = false, columnDefinition = "TEXT") private String signature;
  @Column(nullable = false) private Instant signedAt;

  protected ConsentForm() {}

  public ConsentForm(
      UUID id,
      UUID appointmentId,
      UUID patientId,
      String formType,
      String signature,
      Instant signedAt) {
    this.id = id;
    this.appointmentId = appointmentId;
    this.patientId = patientId;
    this.formType = formType;
    this.signature = signature;
    this.signedAt = signedAt;
  }

  public UUID id() { return id; }
  public UUID getId() { return id; }
  public UUID appointmentId() { return appointmentId; }
  public UUID getAppointmentId() { return appointmentId; }
  public UUID patientId() { return patientId; }
  public UUID getPatientId() { return patientId; }
  public String formType() { return formType; }
  public String getFormType() { return formType; }
  public String signature() { return signature; }
  public String getSignature() { return signature; }
  public Instant signedAt() { return signedAt; }
  public Instant getSignedAt() { return signedAt; }
}
