package git.jogindermikael.ehrservice.controller;

import git.jogindermikael.ehrservice.dto.ClinicalAmendmentRequest;
import git.jogindermikael.ehrservice.dto.ClinicalResourceRequest;
import git.jogindermikael.ehrservice.dto.TerminologyCodeRequest;
import git.jogindermikael.ehrservice.model.ClinicalResource;
import git.jogindermikael.ehrservice.model.TerminologyCode;
import git.jogindermikael.ehrservice.service.ClinicalResourceService;
import git.jogindermikael.ehrservice.dto.ClinicalResourceBundle;
import git.jogindermikael.ehrservice.dto.IntegrityResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ehr/clinical-resources")
@PreAuthorize("hasAnyRole('ADMIN','CLINICIAN')")
@Tag(name = "Structured clinical resources", description = "Versioned clinical resources, terminology and provenance")
public class ClinicalResourceController {
  private final ClinicalResourceService service;

  public ClinicalResourceController(ClinicalResourceService service) {
    this.service = service;
  }

  @PostMapping("/terminology")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Register an approved terminology code")
  public ResponseEntity<TerminologyCode> registerCode(@Valid @RequestBody TerminologyCodeRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.registerCode(request));
  }

  @PostMapping
  @Operation(summary = "Create a structured, terminology-validated clinical resource")
  public ResponseEntity<ClinicalResourceBundle> create(@Valid @RequestBody ClinicalResourceRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
  }

  @PostMapping("/{id}/amendments")
  @Operation(summary = "Append an immutable amendment and provenance entry")
  public ClinicalResourceBundle amend(
      @PathVariable UUID id, @Valid @RequestBody ClinicalAmendmentRequest request) {
    return service.amend(id, request);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get the current resource with version and provenance")
  public ClinicalResourceBundle get(@PathVariable UUID id) {
    return service.get(id);
  }

  @GetMapping
  @Operation(summary = "List a patient's structured resources with pagination")
  public Page<ClinicalResource> list(
      @RequestParam UUID patientId,
      @RequestParam(required = false) String type,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {
    return service.list(patientId, type, page, size);
  }

  @GetMapping("/{id}/integrity")
  @Operation(summary = "Verify the resource's append-only SHA-256 version chain")
  public IntegrityResult verify(@PathVariable UUID id) {
    return service.verifyIntegrity(id);
  }
}
