package git.jogindermikael.patientservice.controller;

import git.jogindermikael.patientservice.model.Patient;
import git.jogindermikael.patientservice.model.PatientStatus;
import git.jogindermikael.patientservice.repository.PatientRepository;
import git.jogindermikael.patientservice.service.FhirPrivacyClient;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(value="/fhir", produces="application/fhir+json")
@PreAuthorize("hasAnyRole('ADMIN','CLINICIAN')")
public class FhirPatientController {
    private final PatientRepository patients;
    private final FhirPrivacyClient privacy;
    public FhirPatientController(PatientRepository patients, FhirPrivacyClient privacy) { this.patients = patients; this.privacy = privacy; }

    @GetMapping("/metadata")
    public Map<String,Object> metadata() {
        return Map.of("resourceType", "CapabilityStatement", "status", "active", "date", "2026-09-08",
                "kind", "instance", "fhirVersion", "4.0.1", "format", List.of("json"),
                "implementation", Map.of("description", "Patient Management read-only Patient endpoint; consent required"),
                "rest", List.of(Map.of("mode", "server", "resource", List.of(Map.of("type", "Patient",
                        "interaction", List.of(Map.of("code", "read")), "versioning", "no-version", "readHistory", false,
                        "updateCreate", false, "conditionalCreate", false, "conditionalRead", "not-supported", "conditionalUpdate", false,
                        "conditionalDelete", "not-supported")))));
    }

    @GetMapping("/Patient/{id}")
    public ResponseEntity<Map<String,Object>> read(@PathVariable String id,
            @RequestHeader(value="X-Break-Glass-Id", required=false) UUID emergency,
            JwtAuthenticationToken token) {
        UUID patientId;
        try { patientId = UUID.fromString(id); }
        catch (IllegalArgumentException exception) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Patient identifier"); }
        privacy.requireAccess(patientId, emergency, token);
        Patient patient = patients.findById(patientId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        if (patient.getStatus() != PatientStatus.ACTIVE) throw new ResponseStatusException(HttpStatus.GONE, "Patient is inactive");
        Map<String,Object> resource = new LinkedHashMap<>();
        resource.put("resourceType", "Patient");
        resource.put("id", id);
        resource.put("meta", Map.of("versionId", Long.toString(patient.getVersion()), "lastUpdated", patient.getUpdatedAt().toString()));
        resource.put("active", true);
        resource.put("identifier", List.of(Map.of("system", "urn:patient-management:mrn", "value", patient.getMrn())));
        resource.put("name", List.of(Map.of("text", patient.getName())));
        resource.put("birthDate", patient.getDateOfBirth().toString());
        resource.put("address", List.of(Map.of("text", patient.getAddress())));
        resource.put("telecom", List.of(Map.of("system", "email", "value", patient.getEmail())));
        String gender = patient.getGender();
        if (gender != null && Set.of("male", "female", "other", "unknown").contains(gender.toLowerCase(Locale.ROOT))) resource.put("gender", gender.toLowerCase(Locale.ROOT));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).eTag("W/\"" + patient.getVersion() + "\"")
                .lastModified(patient.getUpdatedAt()).body(resource);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,Object>> error(ResponseStatusException exception) {
        String code = switch(exception.getStatusCode().value()) {
            case 400 -> "invalid"; case 403 -> "forbidden"; case 404, 410 -> "not-found"; default -> "transient";
        };
        return ResponseEntity.status(exception.getStatusCode()).contentType(MediaType.parseMediaType("application/fhir+json"))
                .cacheControl(CacheControl.noStore()).body(Map.of("resourceType", "OperationOutcome", "issue",
                        List.of(Map.of("severity", "error", "code", code, "diagnostics", Objects.requireNonNullElse(exception.getReason(), "Request failed")))));
    }
}
