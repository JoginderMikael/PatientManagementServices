package git.jogindermikael.auditcomplianceservice.controller;

import git.jogindermikael.auditcomplianceservice.dto.Consent;
import git.jogindermikael.auditcomplianceservice.dto.Emergency;
import git.jogindermikael.auditcomplianceservice.dto.Review;
import git.jogindermikael.auditcomplianceservice.service.PrivacyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.security.Principal;
import java.time.Instant;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/compliance/privacy")
public class PrivacyController {
    private final PrivacyService service;
    public PrivacyController(PrivacyService service) { this.service = service; }
    @PostMapping("/consents")
    @PreAuthorize("hasRole('PRIVACY_OFFICER')")
    public Map<String,UUID> consent(@Valid @RequestBody Consent body, Principal actor) {
        return Map.of("id", service.grant(body.patientId(), body.subject(), body.expiresAt(), body.evidenceReference(), actor.getName(), false));
    }
    @PostMapping("/break-glass")
    @PreAuthorize("hasRole('CLINICIAN')")
    public Map<String,UUID> emergency(@Valid @RequestBody Emergency body, Principal actor) {
        return Map.of("id", service.grant(body.patientId(), actor.getName(), body.expiresAt(), body.evidenceReference(), actor.getName(), true));
    }
    @PostMapping("/grants/{id}/revoke")
    @PreAuthorize("hasRole('PRIVACY_OFFICER')")
    public void revoke(@PathVariable UUID id, Principal actor) { service.revoke(id, actor.getName()); }

    @PostMapping("/break-glass/{id}/review")
    @PreAuthorize("hasRole('PRIVACY_OFFICER')")
    public void review(@PathVariable UUID id, @Valid @RequestBody Review body, Principal actor) { service.review(id, actor.getName(), body.evidenceReference()); }

    @GetMapping("/break-glass/reviews")
    @PreAuthorize("hasRole('PRIVACY_OFFICER')")
    public List<Map<String,Object>> reviews() { return service.pendingReviews(); }

    @PostMapping("/decisions/{patientId}")
    @PreAuthorize("hasAnyRole('CLINICIAN','ADMIN')")
    public Map<String,Boolean> decision(@PathVariable UUID patientId, @RequestParam(required=false) UUID emergencyId, Principal actor) {
        return Map.of("allowed", service.decide(patientId, actor.getName(), emergencyId));
    }
}
