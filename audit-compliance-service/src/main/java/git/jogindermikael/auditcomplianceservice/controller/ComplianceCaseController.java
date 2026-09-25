package git.jogindermikael.auditcomplianceservice.controller;

import git.jogindermikael.auditcomplianceservice.dto.Create;
import git.jogindermikael.auditcomplianceservice.dto.Hold;
import git.jogindermikael.auditcomplianceservice.dto.Transition;
import git.jogindermikael.auditcomplianceservice.service.ComplianceCaseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.security.Principal;
import java.time.Instant;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/compliance/cases")
@PreAuthorize("hasRole('PRIVACY_OFFICER')")
public class ComplianceCaseController {
    private final ComplianceCaseService service;
    public ComplianceCaseController(ComplianceCaseService service) { this.service = service; }
    @PostMapping
    public Map<String,UUID> create(@Valid @RequestBody Create body, Principal actor) {
        return Map.of("id", service.create(body.kind(), body.owner(), body.evidenceReference(), body.dueAt(), actor.getName()));
    }
    @GetMapping
    public List<Map<String,Object>> list(@RequestParam(defaultValue="false") boolean overdue) { return service.list(overdue); }
    @GetMapping("/{id}/history")
    public List<Map<String,Object>> history(@PathVariable UUID id) { return service.history(id); }
    @PostMapping("/{id}/transitions")
    public void transition(@PathVariable UUID id, @Valid @RequestBody Transition body, Principal actor) {
        service.transition(id, body.expectedStatus(), body.status(), body.evidenceReference(), actor.getName());
    }
    @PostMapping("/{id}/hold")
    public void hold(@PathVariable UUID id, @Valid @RequestBody Hold body, Principal actor) {
        service.hold(id, body.enabled(), body.evidenceReference(), actor.getName());
    }
}
