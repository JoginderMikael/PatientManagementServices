package git.jogindermikael.ehrservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class EhrModels {
    private EhrModels() {}

    @Entity @Table(name="encounter")
    public static class Encounter {
        @Id private UUID id; @Column(nullable=false) private UUID patientId; @Column(nullable=false) private UUID clinicianId;
        @Column(nullable=false) private Instant startedAt; private Instant endedAt; @Column(nullable=false) private String status;
        @Column(nullable=false) private String reason; @Column(nullable=false) private Instant createdAt; @Version private long version;
        protected Encounter() {}
        public Encounter(UUID id,UUID patientId,UUID clinicianId,Instant startedAt,String status,String reason,Instant createdAt){this.id=id;this.patientId=patientId;this.clinicianId=clinicianId;this.startedAt=startedAt;this.status=status;this.reason=reason;this.createdAt=createdAt;}
        public UUID id(){return id;} public UUID getId(){return id;} public UUID patientId(){return patientId;} public UUID getPatientId(){return patientId;}
        public UUID clinicianId(){return clinicianId;} public UUID getClinicianId(){return clinicianId;} public Instant startedAt(){return startedAt;} public Instant getStartedAt(){return startedAt;}
        public Instant getEndedAt(){return endedAt;} public String status(){return status;} public String getStatus(){return status;} public String reason(){return reason;} public String getReason(){return reason;}
        public Instant getCreatedAt(){return createdAt;} public long getVersion(){return version;} public void close(){endedAt=Instant.now();status="FINISHED";}
    }

    @Entity @Table(name="medical_history")
    public static class MedicalHistory {
        @Id private UUID id; @Column(nullable=false) private UUID patientId; @Column(nullable=false,columnDefinition="TEXT") private String summary;
        @Convert(converter=StringListConverter.class) @Column(nullable=false,columnDefinition="TEXT") private List<String> allergies;
        @Convert(converter=StringListConverter.class) @Column(nullable=false,columnDefinition="TEXT") private List<String> chronicConditions;
        @Column(nullable=false) private Instant createdAt;
        protected MedicalHistory() {}
        public MedicalHistory(UUID id,UUID patientId,String summary,List<String> allergies,List<String> chronicConditions,Instant createdAt){this.id=id;this.patientId=patientId;this.summary=summary;this.allergies=allergies;this.chronicConditions=chronicConditions;this.createdAt=createdAt;}
        public UUID id(){return id;} public UUID getId(){return id;} public UUID patientId(){return patientId;} public UUID getPatientId(){return patientId;} public String summary(){return summary;} public String getSummary(){return summary;}
        public List<String> allergies(){return allergies;} public List<String> getAllergies(){return allergies;} public List<String> chronicConditions(){return chronicConditions;} public List<String> getChronicConditions(){return chronicConditions;}
        public Instant createdAt(){return createdAt;} public Instant getCreatedAt(){return createdAt;}
    }

    @Entity @Table(name="diagnosis")
    public static class Diagnosis {
        @Id private UUID id; @Column(nullable=false) private UUID patientId; private UUID encounterId; @Column(nullable=false) private UUID clinicianId;
        @Column(nullable=false) private String code; @Column(nullable=false) private String description; @Column(nullable=false) private LocalDate diagnosedOn; @Column(nullable=false) private Instant createdAt;
        protected Diagnosis() {}
        public Diagnosis(UUID id,UUID patientId,UUID encounterId,UUID clinicianId,String code,String description,LocalDate diagnosedOn,Instant createdAt){this.id=id;this.patientId=patientId;this.encounterId=encounterId;this.clinicianId=clinicianId;this.code=code;this.description=description;this.diagnosedOn=diagnosedOn;this.createdAt=createdAt;}
        public UUID id(){return id;} public UUID getId(){return id;} public UUID patientId(){return patientId;} public UUID getPatientId(){return patientId;} public UUID getEncounterId(){return encounterId;}
        public UUID clinicianId(){return clinicianId;} public UUID getClinicianId(){return clinicianId;} public String code(){return code;} public String getCode(){return code;} public String description(){return description;} public String getDescription(){return description;}
        public LocalDate diagnosedOn(){return diagnosedOn;} public LocalDate getDiagnosedOn(){return diagnosedOn;} public Instant createdAt(){return createdAt;} public Instant getCreatedAt(){return createdAt;}
    }

    @Entity @Table(name="prescription")
    public static class Prescription {
        @Id private UUID id; @Column(nullable=false) private UUID patientId; private UUID encounterId; @Column(nullable=false) private UUID clinicianId;
        @Column(nullable=false) private String medication; @Column(nullable=false) private String dosage; @Column(nullable=false,columnDefinition="TEXT") private String instructions;
        @Column(nullable=false) private String status; @Column(nullable=false) private Instant createdAt;
        protected Prescription() {}
        public Prescription(UUID id,UUID patientId,UUID encounterId,UUID clinicianId,String medication,String dosage,String instructions,String status,Instant createdAt){this.id=id;this.patientId=patientId;this.encounterId=encounterId;this.clinicianId=clinicianId;this.medication=medication;this.dosage=dosage;this.instructions=instructions;this.status=status;this.createdAt=createdAt;}
        public UUID id(){return id;} public UUID getId(){return id;} public UUID patientId(){return patientId;} public UUID getPatientId(){return patientId;} public UUID getEncounterId(){return encounterId;}
        public UUID clinicianId(){return clinicianId;} public UUID getClinicianId(){return clinicianId;} public String medication(){return medication;} public String getMedication(){return medication;} public String dosage(){return dosage;} public String getDosage(){return dosage;}
        public String instructions(){return instructions;} public String getInstructions(){return instructions;} public String status(){return status;} public String getStatus(){return status;} public Instant createdAt(){return createdAt;} public Instant getCreatedAt(){return createdAt;}
    }

    @Entity @Table(name="lab_result")
    public static class LabResult {
        @Id private UUID id; @Column(nullable=false) private UUID patientId; private UUID encounterId; @Column(nullable=false) private String testName;
        @Column(nullable=false,columnDefinition="TEXT") private String resultSummary; @Column(nullable=false) private String source; @Column(nullable=false) private LocalDate collectedOn; @Column(nullable=false) private Instant importedAt;
        protected LabResult() {}
        public LabResult(UUID id,UUID patientId,UUID encounterId,String testName,String resultSummary,String source,LocalDate collectedOn,Instant importedAt){this.id=id;this.patientId=patientId;this.encounterId=encounterId;this.testName=testName;this.resultSummary=resultSummary;this.source=source;this.collectedOn=collectedOn;this.importedAt=importedAt;}
        public UUID id(){return id;} public UUID getId(){return id;} public UUID patientId(){return patientId;} public UUID getPatientId(){return patientId;} public UUID getEncounterId(){return encounterId;}
        public String testName(){return testName;} public String getTestName(){return testName;} public String resultSummary(){return resultSummary;} public String getResultSummary(){return resultSummary;} public String source(){return source;} public String getSource(){return source;}
        public LocalDate collectedOn(){return collectedOn;} public LocalDate getCollectedOn(){return collectedOn;} public Instant importedAt(){return importedAt;} public Instant getImportedAt(){return importedAt;}
    }

    @Entity @Table(name="clinical_note")
    public static class ClinicalNote {
        @Id private UUID id; @Column(nullable=false) private UUID encounterId; @Column(nullable=false) private UUID patientId; @Column(nullable=false) private UUID clinicianId;
        @Column(nullable=false,columnDefinition="TEXT") private String body; @Column(nullable=false) private String status; private Instant signedAt;
        @Column(nullable=false) private Instant createdAt; @Column(nullable=false) private Instant updatedAt; @Version private long version;
        protected ClinicalNote() {}
        public ClinicalNote(UUID id,UUID encounterId,UUID patientId,UUID clinicianId,String body){this.id=id;this.encounterId=encounterId;this.patientId=patientId;this.clinicianId=clinicianId;this.body=body;this.status="DRAFT";this.createdAt=Instant.now();this.updatedAt=createdAt;}
        public UUID getId(){return id;} public UUID getEncounterId(){return encounterId;} public UUID getPatientId(){return patientId;} public UUID getClinicianId(){return clinicianId;}
        public String getBody(){return body;} public String getStatus(){return status;} public Instant getSignedAt(){return signedAt;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;} public long getVersion(){return version;}
        public void sign(){status="SIGNED";signedAt=Instant.now();updatedAt=signedAt;}
    }

    @Entity @Table(name="vaccination_record")
    public static class VaccinationRecord {
        @Id private UUID id; @Column(nullable=false) private UUID patientId; @Column(nullable=false) private String vaccine; @Column(nullable=false) private LocalDate administeredOn; @Column(nullable=false) private String lotNumber; @Column(nullable=false) private Instant createdAt;
        protected VaccinationRecord() {}
        public VaccinationRecord(UUID id,UUID patientId,String vaccine,LocalDate administeredOn,String lotNumber,Instant createdAt){this.id=id;this.patientId=patientId;this.vaccine=vaccine;this.administeredOn=administeredOn;this.lotNumber=lotNumber;this.createdAt=createdAt;}
        public UUID id(){return id;} public UUID getId(){return id;} public UUID patientId(){return patientId;} public UUID getPatientId(){return patientId;} public String vaccine(){return vaccine;} public String getVaccine(){return vaccine;}
        public LocalDate administeredOn(){return administeredOn;} public LocalDate getAdministeredOn(){return administeredOn;} public String lotNumber(){return lotNumber;} public String getLotNumber(){return lotNumber;} public Instant createdAt(){return createdAt;} public Instant getCreatedAt(){return createdAt;}
    }
}
