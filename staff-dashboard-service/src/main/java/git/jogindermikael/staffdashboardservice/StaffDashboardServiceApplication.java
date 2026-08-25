package git.jogindermikael.staffdashboardservice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class StaffDashboardServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StaffDashboardServiceApplication.class, args);
    }
}

@RestController
@RequestMapping("/staff-dashboard")
@Tag(name = "Doctor/Staff Dashboard", description = "Clinical staff rounds, patient charts and task management")
class StaffDashboardController {
    private final Map<UUID, ClinicalRound> rounds = new ConcurrentHashMap<>();
    private final Map<UUID, PatientChartSummary> charts = new ConcurrentHashMap<>();
    private final Map<UUID, StaffTask> tasks = new ConcurrentHashMap<>();

    @GetMapping("/daily/{staffId}")
    @Operation(summary = "Get a daily dashboard for a staff member")
    StaffDashboard dailyDashboard(@PathVariable UUID staffId) {
        return new StaffDashboard(staffId, LocalDate.now(), rounds.values().stream().filter(item -> item.staffId().equals(staffId)).toList(), tasks.values().stream().filter(item -> item.assigneeId().equals(staffId)).toList());
    }

    @PostMapping("/rounds")
    @Operation(summary = "Create a clinical round entry")
    ResponseEntity<ClinicalRound> createRound(@Valid @RequestBody ClinicalRoundRequest request) {
        UUID id = UUID.randomUUID();
        ClinicalRound round = new ClinicalRound(id, request.staffId(), request.patientId(), request.unit(), request.notes(), "OPEN", Instant.now());
        rounds.put(id, round);
        return ResponseEntity.status(HttpStatus.CREATED).body(round);
    }

    @PostMapping("/charts")
    @Operation(summary = "Create or update a patient chart summary")
    ResponseEntity<PatientChartSummary> upsertChart(@Valid @RequestBody PatientChartRequest request) {
        UUID id = request.id() == null ? UUID.randomUUID() : request.id();
        PatientChartSummary chart = new PatientChartSummary(id, request.patientId(), request.summary(), request.riskLevel(), Instant.now());
        charts.put(id, chart);
        return ResponseEntity.status(HttpStatus.CREATED).body(chart);
    }

    @GetMapping("/charts/{patientId}")
    @Operation(summary = "Get chart summaries for a patient")
    List<PatientChartSummary> chartsForPatient(@PathVariable UUID patientId) {
        return charts.values().stream().filter(chart -> chart.patientId().equals(patientId)).toList();
    }

    @PostMapping("/tasks")
    @Operation(summary = "Create a staff task")
    ResponseEntity<StaffTask> createTask(@Valid @RequestBody StaffTaskRequest request) {
        UUID id = UUID.randomUUID();
        StaffTask task = new StaffTask(id, request.assigneeId(), request.patientId(), request.title(), request.priority(), "OPEN", Instant.now());
        tasks.put(id, task);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PostMapping("/tasks/{id}/complete")
    @Operation(summary = "Complete a staff task")
    StaffTask completeTask(@PathVariable UUID id) {
        StaffTask task = Optional.ofNullable(tasks.get(id)).orElseThrow();
        StaffTask completed = new StaffTask(task.id(), task.assigneeId(), task.patientId(), task.title(), task.priority(), "COMPLETED", Instant.now());
        tasks.put(id, completed);
        return completed;
    }
}

record StaffDashboard(UUID staffId, LocalDate date, List<ClinicalRound> rounds, List<StaffTask> tasks) {}
record ClinicalRound(UUID id, UUID staffId, UUID patientId, String unit, String notes, String status, Instant updatedAt) {}
record PatientChartSummary(UUID id, UUID patientId, String summary, String riskLevel, Instant updatedAt) {}
record StaffTask(UUID id, UUID assigneeId, UUID patientId, String title, String priority, String status, Instant updatedAt) {}

record ClinicalRoundRequest(@NotNull UUID staffId, @NotNull UUID patientId, @NotBlank String unit, @NotBlank String notes) {}
record PatientChartRequest(UUID id, @NotNull UUID patientId, @NotBlank String summary, @NotBlank String riskLevel) {}
record StaffTaskRequest(@NotNull UUID assigneeId, @NotNull UUID patientId, @NotBlank String title, @NotBlank String priority) {}
