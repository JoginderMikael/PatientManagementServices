package git.jogindermikael.patientservice.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "patient")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @NotNull
    private String name;
    @NotNull
    @Email
    @Column(unique = true)
    private String email;
    @NotNull
    private String address;
    @NotNull
    private LocalDate dateOfBirth;
    @NotNull
    private LocalDate registeredDate;
    @Column(nullable = false, unique = true, length = 64)
    private String mrn;
    @Column(length = 40)
    private String phone;
    @Column(length = 40)
    private String gender;
    @Column(length = 20)
    private String preferredLanguage;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private PatientStatus status = PatientStatus.ACTIVE;
    private UUID mergedIntoPatientId;
    @Column(unique = true, length = 100)
    private String registrationKey;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "patient_identifier", joinColumns = @JoinColumn(name = "patient_id"))
    @MapKeyColumn(name = "identifier_system")
    @Column(name = "identifier_value", nullable = false)
    private Map<String, String> externalIdentifiers = new LinkedHashMap<>();
    @Version
    private long version;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public LocalDate getRegisteredDate() { return registeredDate; }
    public void setRegisteredDate(LocalDate registeredDate) { this.registeredDate = registeredDate; }
    public String getMrn() { return mrn; }
    public void setMrn(String mrn) { this.mrn = mrn; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    public PatientStatus getStatus() { return status; }
    public void setStatus(PatientStatus status) { this.status = status; }
    public UUID getMergedIntoPatientId() { return mergedIntoPatientId; }
    public void setMergedIntoPatientId(UUID mergedIntoPatientId) { this.mergedIntoPatientId = mergedIntoPatientId; }
    public String getRegistrationKey() { return registrationKey; }
    public void setRegistrationKey(String registrationKey) { this.registrationKey = registrationKey; }
    public Map<String, String> getExternalIdentifiers() { return externalIdentifiers; }
    public void setExternalIdentifiers(Map<String, String> identifiers) {
        externalIdentifiers = identifiers == null ? new LinkedHashMap<>() : new LinkedHashMap<>(identifiers);
    }
    public long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
