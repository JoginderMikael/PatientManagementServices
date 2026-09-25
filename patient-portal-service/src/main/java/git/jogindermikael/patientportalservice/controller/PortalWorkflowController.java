package git.jogindermikael.patientportalservice.controller;

import git.jogindermikael.patientportalservice.dto.AppointmentResolution;
import git.jogindermikael.patientportalservice.dto.Grant;
import git.jogindermikael.patientportalservice.dto.Identity;
import git.jogindermikael.patientportalservice.service.PortalWorkflowService;
import git.jogindermikael.patientportalservice.dto.Release;
import git.jogindermikael.patientportalservice.dto.Settlement;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal")
@PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
public class PortalWorkflowController {
  private final PortalWorkflowService service;

  public PortalWorkflowController(PortalWorkflowService service) {
    this.service = service;
  }

  @PostMapping("/identities")
  @PreAuthorize("hasRole('ADMIN')")
  public void bind(@Valid @RequestBody Identity c) {
    service.bind(c);
  }

  @PostMapping("/patients/{id}/proxies")
  public UUID grant(@PathVariable UUID id, @Valid @RequestBody Grant c) {
    return service.grant(id, c);
  }

  @DeleteMapping("/patients/{patient}/proxies/{id}")
  public void revoke(@PathVariable UUID patient, @PathVariable UUID id) {
    service.revoke(patient, id);
  }

  @GetMapping("/patients/{id}/requests")
  public List<Map<String, Object>> requests(@PathVariable UUID id, @RequestParam String type) {
    return service.requests(id, type);
  }

  @PostMapping("/appointment-requests/{id}/cancel")
  public void cancel(@PathVariable UUID id) {
    service.cancelAppointment(id);
  }

  @PostMapping("/appointment-requests/{id}/resolve")
  @PreAuthorize("hasRole('ADMIN')")
  public void resolve(
      @PathVariable UUID id, @Valid @RequestBody AppointmentResolution c) {
    service.resolveAppointment(id, c);
  }

  @PostMapping("/record-requests/{id}/release")
  @PreAuthorize("hasRole('ADMIN')")
  public void release(@PathVariable UUID id, @Valid @RequestBody Release c) {
    service.release(id, c);
  }

  @GetMapping("/record-requests/{id}/download")
  public ResponseEntity<String> download(@PathVariable UUID id) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .header("Content-Disposition", "attachment; filename=records.txt")
        .contentType(MediaType.TEXT_PLAIN)
        .body(service.download(id));
  }

  @PostMapping("/payments/{id}/settle")
  @PreAuthorize("hasRole('ADMIN')")
  public Map<String, Object> settle(
      @PathVariable UUID id, @Valid @RequestBody Settlement c) {
    return service.settle(id, c);
  }
}
