package git.jogindermikael.staffdashboardservice.controller;

import git.jogindermikael.staffdashboardservice.service.TaskWorkflowService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/staff-dashboard")
@PreAuthorize(
    "hasAnyRole('ADMIN','CLINICIAN','NURSE','RECEPTIONIST','BILLING_STAFF','PHARMACIST','LAB_STAFF')")
public class TaskWorkflowController {
  private final TaskWorkflowService service;

  public TaskWorkflowController(TaskWorkflowService service) {
    this.service = service;
  }

  @GetMapping("/queues/{role}")
  public List<Map<String, Object>> queue(
      @PathVariable String role, @RequestParam(defaultValue = "50") int limit) {
    return service.queue(role, limit);
  }

  @PostMapping("/tasks/{id}/claim")
  public void claim(@PathVariable UUID id) {
    service.claim(id);
  }

  @PostMapping("/tasks/{id}/handoff")
  public void handoff(@PathVariable UUID id, @Valid @RequestBody TaskWorkflowService.Handoff c) {
    service.handoff(id, c);
  }

  @PostMapping("/tasks/{id}/comments")
  public void comment(@PathVariable UUID id, @Valid @RequestBody TaskWorkflowService.Comment c) {
    service.comment(id, c);
  }

  @GetMapping("/tasks/{id}/history")
  public List<Map<String, Object>> history(@PathVariable UUID id) {
    return service.history(id);
  }

  @PostMapping("/tasks/escalate")
  @PreAuthorize("hasRole('ADMIN')")
  public int escalate() {
    return service.escalate();
  }

  @PostMapping("/clinical-tasks")
  @PreAuthorize("hasAnyRole('ADMIN','CLINICIAN','NURSE')")
  public Map<String, Object> clinical(@Valid @RequestBody TaskWorkflowService.ClinicalTask c) {
    return service.clinical(c);
  }
}
