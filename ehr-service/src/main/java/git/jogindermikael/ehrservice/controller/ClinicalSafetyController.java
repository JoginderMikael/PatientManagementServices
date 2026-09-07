package git.jogindermikael.ehrservice.controller;

import git.jogindermikael.ehrservice.service.ClinicalSafetyService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ehr/safety")
@PreAuthorize("hasAnyRole('ADMIN','CLINICIAN','NURSE')")
public class ClinicalSafetyController {
  private final ClinicalSafetyService service;

  public ClinicalSafetyController(ClinicalSafetyService service) {
    this.service = service;
  }

  @PostMapping("/rules")
  @PreAuthorize("hasRole('ADMIN')")
  public UUID rule(@Valid @RequestBody ClinicalSafetyService.Rule c) {
    return service.rule(c);
  }

  @PostMapping("/alerts")
  public Map<String, Object> alert(@Valid @RequestBody ClinicalSafetyService.Alert c) {
    return service.alert(c);
  }

  @GetMapping("/alerts/patient/{id}")
  public List<Map<String, Object>> alerts(@PathVariable UUID id) {
    return service.alerts(id);
  }

  @PostMapping("/alerts/{id}/acknowledge")
  public void acknowledge(@PathVariable UUID id) {
    service.acknowledge(id);
  }

  @PostMapping("/alerts/{id}/escalate")
  public Map<String, Object> escalate(@PathVariable UUID id) {
    return service.escalate(id);
  }

  @GetMapping("/prescriptions/{id}/dispensing")
  @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
  public Map<String, Object> dispensing(@PathVariable UUID id) {
    return service.dispensingSafety(id);
  }

  @PostMapping("/prescriptions/{id}/discontinue")
  @PreAuthorize("hasAnyRole('ADMIN','CLINICIAN')")
  public void discontinue(@PathVariable UUID id) {
    service.discontinue(id);
  }
}
