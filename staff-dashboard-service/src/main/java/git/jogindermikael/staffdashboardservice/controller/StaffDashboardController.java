package git.jogindermikael.staffdashboardservice.controller;

import git.jogindermikael.staffdashboardservice.dto.*;
import git.jogindermikael.staffdashboardservice.model.*;
import git.jogindermikael.staffdashboardservice.service.StaffDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize(
    "hasAnyRole('ADMIN','CLINICIAN','NURSE','RECEPTIONIST','BILLING_STAFF','PHARMACIST','LAB_STAFF')")
@RequestMapping("/staff-dashboard")
@Tag(
    name = "Doctor/Staff Dashboard",
    description = "Clinical staff rounds, patient charts and task management")
public class StaffDashboardController {
  private final StaffDashboardService staffDashboardService;

  public StaffDashboardController(StaffDashboardService staffDashboardService) {
    this.staffDashboardService = staffDashboardService;
  }

  @GetMapping("/daily/{staffId}")
  @Operation(summary = "Get a daily dashboard for a staff member")
  public StaffDashboard dailyDashboard(@PathVariable UUID staffId) {
    return staffDashboardService.dailyDashboard(staffId);
  }

  @PostMapping("/rounds")
  @PreAuthorize("hasAnyRole('ADMIN','CLINICIAN','NURSE')")
  @Operation(summary = "Create a clinical round entry")
  public ResponseEntity<ClinicalRound> createRound(
      @Valid @RequestBody ClinicalRoundRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(staffDashboardService.createRound(request));
  }

  @PostMapping("/charts")
  @PreAuthorize("hasAnyRole('ADMIN','CLINICIAN','NURSE')")
  @Operation(summary = "Create or update a patient chart summary")
  public ResponseEntity<PatientChartSummary> upsertChart(
      @Valid @RequestBody PatientChartRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(staffDashboardService.upsertChart(request));
  }

  @GetMapping("/charts/{patientId}")
  @PreAuthorize("hasAnyRole('ADMIN','CLINICIAN','NURSE')")
  @Operation(summary = "Get chart summaries for a patient")
  public List<PatientChartSummary> chartsForPatient(@PathVariable UUID patientId) {
    return staffDashboardService.chartsForPatient(patientId);
  }

  @PostMapping("/tasks")
  @Operation(summary = "Create a staff task")
  public ResponseEntity<StaffTask> createTask(@Valid @RequestBody StaffTaskRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(staffDashboardService.createTask(request));
  }

  @PostMapping("/tasks/{id}/complete")
  @Operation(summary = "Complete a staff task")
  public StaffTask completeTask(@PathVariable UUID id) {
    return staffDashboardService.completeTask(id);
  }
}
