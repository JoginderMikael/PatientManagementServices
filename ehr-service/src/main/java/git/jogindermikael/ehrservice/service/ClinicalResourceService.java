package git.jogindermikael.ehrservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import git.jogindermikael.ehrservice.dto.ClinicalAmendmentRequest;
import git.jogindermikael.ehrservice.dto.ClinicalResourceBundle;
import git.jogindermikael.ehrservice.dto.ClinicalResourceRequest;
import git.jogindermikael.ehrservice.dto.IntegrityResult;
import git.jogindermikael.ehrservice.dto.TerminologyCodeRequest;
import git.jogindermikael.ehrservice.model.*;
import git.jogindermikael.ehrservice.model.Encounter;
import git.jogindermikael.ehrservice.repository.*;
import git.jogindermikael.reliability.DurableEventOutbox;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ClinicalResourceService {
  public static final Set<String> RESOURCE_TYPES =
      Set.of(
          "OBSERVATION",
          "ALLERGY",
          "PROBLEM",
          "MEDICATION_ADMINISTRATION",
          "ORDER",
          "RESULT",
          "CARE_PLAN",
          "DOCUMENT",
          "IMAGING",
          "REFERRAL",
          "DISCHARGE_SUMMARY");
  private static final Set<String> STATUSES =
      Set.of("DRAFT", "PRELIMINARY", "FINAL", "AMENDED", "CORRECTED", "CANCELLED");

  private final ClinicalResourceRepository resources;
  private final ClinicalResourceVersionRepository versions;
  private final ClinicalProvenanceRepository provenance;
  private final TerminologyCodeRepository terminology;
  private final EncounterRepository encounters;
  private final ObjectMapper mapper;
  private final DurableEventOutbox outbox;

  public ClinicalResourceService(
      ClinicalResourceRepository resources,
      ClinicalResourceVersionRepository versions,
      ClinicalProvenanceRepository provenance,
      TerminologyCodeRepository terminology,
      EncounterRepository encounters,
      ObjectMapper mapper,
      DurableEventOutbox outbox) {
    this.resources = resources;
    this.versions = versions;
    this.provenance = provenance;
    this.terminology = terminology;
    this.encounters = encounters;
    this.mapper = mapper;
    this.outbox = outbox;
  }

  public TerminologyCode registerCode(TerminologyCodeRequest request) {
    Set<String> types =
        request.resourceTypes().stream().map(this::validateType).collect(java.util.stream.Collectors.toSet());
    return terminology.save(
        new TerminologyCode(
            UUID.randomUUID(), request.systemUri(), request.code(), request.display(), types));
  }

  public ClinicalResourceBundle create(ClinicalResourceRequest request) {
    String type = validateType(request.resourceType());
    String status = validateStatus(request.status());
    validateEncounter(request.encounterId(), request.patientId());
    TerminologyCode code = requireCode(request.codeSystem(), request.code(), type);
    if (!code.getDisplay().equalsIgnoreCase(request.display())) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Terminology display does not match registered code");
    }

    String actor = actor();
    ClinicalResource resource =
        resources.save(
            new ClinicalResource(
                UUID.randomUUID(),
                request.patientId(),
                request.encounterId(),
                type,
                status,
                request.codeSystem(),
                request.code(),
                code.getDisplay(),
                request.effectiveAt(),
                actor));
    ClinicalResourceVersion version =
        appendVersion(resource, 1, request.payload(), null, actor, null, "CREATED");
    emit("CLINICAL_RESOURCE_CREATED", resource, version);
    return new ClinicalResourceBundle(resource, version, provenance.findByResourceIdOrderByVersionNumber(resource.getId()));
  }

  public ClinicalResourceBundle amend(UUID id, ClinicalAmendmentRequest request) {
    ClinicalResource resource = requireResource(id);
    String status = validateStatus(request.status());
    ClinicalResourceVersion prior =
        versions
            .findByResourceIdAndVersionNumber(id, resource.getCurrentVersion())
            .orElseThrow(() -> new IllegalStateException("Current clinical resource version is missing"));
    int next = resource.amend(status);
    ClinicalResourceVersion version =
        appendVersion(resource, next, request.payload(), request.reason(), actor(), prior.getContentHash(), "AMENDED");
    emit("CLINICAL_RESOURCE_AMENDED", resource, version);
    return new ClinicalResourceBundle(resource, version, provenance.findByResourceIdOrderByVersionNumber(id));
  }

  @Transactional(readOnly = true)
  public ClinicalResourceBundle get(UUID id) {
    ClinicalResource resource = requireResource(id);
    ClinicalResourceVersion current =
        versions
            .findByResourceIdAndVersionNumber(id, resource.getCurrentVersion())
            .orElseThrow(() -> new IllegalStateException("Current clinical resource version is missing"));
    return new ClinicalResourceBundle(
        resource, current, provenance.findByResourceIdOrderByVersionNumber(id));
  }

  @Transactional(readOnly = true)
  public Page<ClinicalResource> list(UUID patientId, String type, int page, int size) {
    PageRequest paging = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 200)));
    if (type == null || type.isBlank()) return resources.findByPatientId(patientId, paging);
    return resources.findByPatientIdAndResourceType(patientId, validateType(type), paging);
  }

  @Transactional(readOnly = true)
  public IntegrityResult verifyIntegrity(UUID id) {
    List<ClinicalResourceVersion> chain = versions.findByResourceIdOrderByVersionNumber(id);
    String previous = null;
    for (ClinicalResourceVersion version : chain) {
      String expected =
          hash(
              id,
              version.getVersionNumber(),
              version.getPayload(),
              version.getRecordedBy(),
              version.getRecordedAt(),
              previous);
      if (!expected.equals(version.getContentHash())
          || !java.util.Objects.equals(previous, version.getPreviousHash())) {
        return new IntegrityResult(id, false, version.getVersionNumber(), chain.size());
      }
      previous = version.getContentHash();
    }
    return new IntegrityResult(id, !chain.isEmpty(), null, chain.size());
  }

  private ClinicalResourceVersion appendVersion(
      ClinicalResource resource,
      int number,
      Map<String, Object> payload,
      String reason,
      String actor,
      String previousHash,
      String action) {
    // PostgreSQL and H2 persist timestamp values at microsecond precision. Canonicalize before
    // hashing so the stored version can always be verified after a database round trip.
    Instant recordedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    String json = serialize(payload);
    String contentHash = hash(resource.getId(), number, json, actor, recordedAt, previousHash);
    ClinicalResourceVersion version =
        versions.save(
            new ClinicalResourceVersion(
                UUID.randomUUID(),
                resource.getId(),
                number,
                json,
                reason,
                actor,
                recordedAt,
                previousHash,
                contentHash));
    provenance.save(
        new ClinicalProvenance(
            UUID.randomUUID(), resource.getId(), number, action, actor, recordedAt, contentHash));
    return version;
  }

  private void validateEncounter(UUID encounterId, UUID patientId) {
    if (encounterId == null) return;
    Encounter encounter =
        encounters
            .findById(encounterId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Encounter not found"));
    if (!encounter.patientId().equals(patientId)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Encounter belongs to a different patient");
    }
  }

  private TerminologyCode requireCode(String system, String code, String type) {
    TerminologyCode terminologyCode =
        terminology
            .findBySystemUriAndCode(system, code)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY, "Terminology code is not registered"));
    if (!terminologyCode.supports(type)) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "Terminology code is not active for this resource type");
    }
    return terminologyCode;
  }

  private ClinicalResource requireResource(UUID id) {
    return resources
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinical resource not found"));
  }

  private String validateType(String value) {
    String type = value.toUpperCase(Locale.ROOT);
    if (!RESOURCE_TYPES.contains(type)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported clinical resource type");
    }
    return type;
  }

  private String validateStatus(String value) {
    String status = value.toUpperCase(Locale.ROOT);
    if (!STATUSES.contains(status)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported clinical resource status");
    }
    return status;
  }

  private String serialize(Map<String, Object> payload) {
    try {
      return mapper.writer().with(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS).writeValueAsString(payload);
    } catch (JsonProcessingException exception) {
      throw new IllegalArgumentException("Clinical payload is not serializable", exception);
    }
  }

  private String hash(UUID id, int version, String payload, String actor, Instant at, String previous) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      String material =
          id + "|" + version + "|" + payload + "|" + actor + "|" + at + "|" + (previous == null ? "" : previous);
      return HexFormat.of().formatHex(digest.digest(material.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException impossible) {
      throw new IllegalStateException("SHA-256 is unavailable", impossible);
    }
  }

  private String actor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication == null ? "system" : authentication.getName();
  }

  private void emit(String eventType, ClinicalResource resource, ClinicalResourceVersion version) {
    Map<String, Object> event = new LinkedHashMap<>();
    event.put("eventId", UUID.randomUUID());
    event.put("schemaVersion", 1);
    event.put("eventType", eventType);
    event.put("occurredAt", Instant.now());
    event.put("source", "ehr-service");
    event.put("aggregateType", "CLINICAL_RESOURCE");
    event.put("aggregateId", resource.getId());
    event.put("patientId", resource.getPatientId());
    event.put("resourceType", resource.getResourceType());
    event.put("version", version.getVersionNumber());
    event.put("contentHash", version.getContentHash());
    outbox.append("ehr.events.v1", resource.getId().toString(), event);
  }

}
