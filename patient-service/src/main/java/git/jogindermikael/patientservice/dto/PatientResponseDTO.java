package git.jogindermikael.patientservice.dto;

import java.util.Map;

public class PatientResponseDTO {
    private String id;
    private String name;
    private String email;
    private String address;
    private String dateOfBirth;
    private String mrn;
    private String phone;
    private String gender;
    private String preferredLanguage;
    private String status;
    private String mergedIntoPatientId;
    private Map<String, String> externalIdentifiers;
    private long version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public String getMrn() { return mrn; }
    public void setMrn(String mrn) { this.mrn = mrn; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMergedIntoPatientId() { return mergedIntoPatientId; }
    public void setMergedIntoPatientId(String mergedIntoPatientId) { this.mergedIntoPatientId = mergedIntoPatientId; }
    public Map<String, String> getExternalIdentifiers() { return externalIdentifiers; }
    public void setExternalIdentifiers(Map<String, String> externalIdentifiers) { this.externalIdentifiers = externalIdentifiers; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
