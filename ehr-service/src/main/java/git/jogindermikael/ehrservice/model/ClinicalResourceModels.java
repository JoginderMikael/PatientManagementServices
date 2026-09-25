package git.jogindermikael.ehrservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ClinicalResourceModels {
  private ClinicalResourceModels() {}

  @Entity
  @Table(name = "clinical_terminology_code")
  public static class TerminologyCode {
    @Id private UUID id;
    @Column(nullable = false) private String systemUri;
    @Column(nullable = false) private String code;
    @Column(nullable = false) private String display;
    @Column(nullable = false) private String resourceTypes;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false) private Instant updatedAt;
    @Version private long lockVersion;

    protected TerminologyCode() {}

    public TerminologyCode(UUID id, String systemUri, String code, String display, Set<String> types) {
      this.id = id;
      this.systemUri = systemUri;
      this.code = code;
      this.display = display;
      this.resourceTypes = types.stream().map(String::toUpperCase).sorted().collect(Collectors.joining(","));
      this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getSystemUri() { return systemUri; }
    public String getCode() { return code; }
    public String getDisplay() { return display; }
    public String getResourceTypes() { return resourceTypes; }
    public boolean isActive() { return active; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getLockVersion() { return lockVersion; }
    public boolean supports(String type) {
      return active && Arrays.asList(resourceTypes.split(",")).contains(type.toUpperCase());
    }
  }

  @Entity
  @Table(name = "clinical_resource")
  public static class ClinicalResource {
    @Id private UUID id;
    @Column(nullable = false) private UUID patientId;
    private UUID encounterId;
    @Column(nullable = false) private String resourceType;
    @Column(nullable = false) private String status;
    @Column(nullable = false) private String codeSystem;
    @Column(nullable = false) private String code;
    @Column(nullable = false) private String display;
    @Column(nullable = false) private Instant effectiveAt;
    @Column(nullable = false) private int currentVersion;
    @Column(nullable = false) private String createdBy;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;
    @Version private long lockVersion;

    protected ClinicalResource() {}

    public ClinicalResource(UUID id, UUID patientId, UUID encounterId, String type, String status,
        String system, String code, String display, Instant effectiveAt, String actor) {
      this.id=id; this.patientId=patientId; this.encounterId=encounterId; this.resourceType=type;
      this.status=status; this.codeSystem=system; this.code=code; this.display=display;
      this.effectiveAt=effectiveAt; this.currentVersion=1; this.createdBy=actor;
      this.createdAt=Instant.now(); this.updatedAt=createdAt;
    }

    public UUID getId(){return id;} public UUID getPatientId(){return patientId;} public UUID getEncounterId(){return encounterId;}
    public String getResourceType(){return resourceType;} public String getStatus(){return status;} public String getCodeSystem(){return codeSystem;}
    public String getCode(){return code;} public String getDisplay(){return display;} public Instant getEffectiveAt(){return effectiveAt;}
    public int getCurrentVersion(){return currentVersion;} public String getCreatedBy(){return createdBy;} public Instant getCreatedAt(){return createdAt;}
    public Instant getUpdatedAt(){return updatedAt;} public long getLockVersion(){return lockVersion;}
    public int amend(String nextStatus){currentVersion++;status=nextStatus;updatedAt=Instant.now();return currentVersion;}
  }

  @Entity
  @Table(name = "clinical_resource_version")
  public static class ClinicalResourceVersion {
    @Id private UUID id;
    @Column(nullable = false) private UUID resourceId;
    @Column(nullable = false) private int versionNumber;
    @Column(nullable = false, columnDefinition = "TEXT") private String payload;
    private String amendmentReason;
    @Column(nullable = false) private String recordedBy;
    @Column(nullable = false) private Instant recordedAt;
    private String previousHash;
    @Column(nullable = false) private String contentHash;

    protected ClinicalResourceVersion() {}
    public ClinicalResourceVersion(UUID id,UUID resourceId,int versionNumber,String payload,String reason,
        String actor,Instant recordedAt,String previousHash,String contentHash){this.id=id;this.resourceId=resourceId;
      this.versionNumber=versionNumber;this.payload=payload;this.amendmentReason=reason;this.recordedBy=actor;
      this.recordedAt=recordedAt;this.previousHash=previousHash;this.contentHash=contentHash;}
    public UUID getId(){return id;} public UUID getResourceId(){return resourceId;} public int getVersionNumber(){return versionNumber;}
    public String getPayload(){return payload;} public String getAmendmentReason(){return amendmentReason;} public String getRecordedBy(){return recordedBy;}
    public Instant getRecordedAt(){return recordedAt;} public String getPreviousHash(){return previousHash;} public String getContentHash(){return contentHash;}
  }

  @Entity
  @Table(name = "clinical_provenance")
  public static class ClinicalProvenance {
    @Id private UUID id;
    @Column(nullable = false) private UUID resourceId;
    @Column(nullable = false) private int versionNumber;
    @Column(nullable = false) private String action;
    @Column(nullable = false) private String actorId;
    @Column(nullable = false) private Instant occurredAt;
    @Column(nullable = false) private String contentHash;
    protected ClinicalProvenance() {}
    public ClinicalProvenance(UUID id,UUID resourceId,int version,String action,String actor,Instant at,String hash){this.id=id;this.resourceId=resourceId;this.versionNumber=version;this.action=action;this.actorId=actor;this.occurredAt=at;this.contentHash=hash;}
    public UUID getId(){return id;} public UUID getResourceId(){return resourceId;} public int getVersionNumber(){return versionNumber;}
    public String getAction(){return action;} public String getActorId(){return actorId;} public Instant getOccurredAt(){return occurredAt;} public String getContentHash(){return contentHash;}
  }
}
