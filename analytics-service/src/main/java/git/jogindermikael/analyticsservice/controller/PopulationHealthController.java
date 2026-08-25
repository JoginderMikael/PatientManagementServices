package git.jogindermikael.analyticsservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/analytics")
@Tag(name = "Reporting & Population Health", description = "Health trends, chronic disease cohorts and regulatory reporting")
public class PopulationHealthController {
    private final Map<UUID, HealthTrend> trends = new ConcurrentHashMap<>();
    private final Map<UUID, ChronicDiseaseCohort> cohorts = new ConcurrentHashMap<>();
    private final Map<UUID, RegulatoryReport> reports = new ConcurrentHashMap<>();

    @PostMapping("/health-trends")
    @Operation(summary = "Record a population health trend")
    ResponseEntity<HealthTrend> recordTrend(@Valid @RequestBody HealthTrendRequest request) {
        UUID id = UUID.randomUUID();
        HealthTrend trend = new HealthTrend(id, request.metric(), request.segment(), request.periodStart(), request.periodEnd(), request.value(), Instant.now());
        trends.put(id, trend);
        return ResponseEntity.status(HttpStatus.CREATED).body(trend);
    }

    @GetMapping("/health-trends")
    @Operation(summary = "List population health trends")
    List<HealthTrend> listTrends() {
        return trends.values().stream().sorted(Comparator.comparing(HealthTrend::createdAt)).toList();
    }

    @PostMapping("/chronic-disease-cohorts")
    @Operation(summary = "Create a chronic disease cohort")
    ResponseEntity<ChronicDiseaseCohort> createCohort(@Valid @RequestBody ChronicDiseaseCohortRequest request) {
        UUID id = UUID.randomUUID();
        ChronicDiseaseCohort cohort = new ChronicDiseaseCohort(id, request.condition(), request.riskLevel(), request.patientCount(), Instant.now());
        cohorts.put(id, cohort);
        return ResponseEntity.status(HttpStatus.CREATED).body(cohort);
    }

    @GetMapping("/chronic-disease-cohorts")
    @Operation(summary = "List chronic disease cohorts")
    List<ChronicDiseaseCohort> listCohorts() {
        return cohorts.values().stream().sorted(Comparator.comparing(ChronicDiseaseCohort::createdAt)).toList();
    }

    @PostMapping("/regulatory-reports")
    @Operation(summary = "Generate a regulatory report")
    ResponseEntity<RegulatoryReport> generateReport(@Valid @RequestBody RegulatoryReportRequest request) {
        UUID id = UUID.randomUUID();
        RegulatoryReport report = new RegulatoryReport(id, request.reportType(), request.periodStart(), request.periodEnd(), "GENERATED", Instant.now());
        reports.put(id, report);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    @GetMapping("/regulatory-reports")
    @Operation(summary = "List regulatory reports")
    List<RegulatoryReport> listReports() {
        return reports.values().stream().sorted(Comparator.comparing(RegulatoryReport::createdAt)).toList();
    }
}

record HealthTrend(UUID id, String metric, String segment, LocalDate periodStart, LocalDate periodEnd, int value, Instant createdAt) {}
record ChronicDiseaseCohort(UUID id, String condition, String riskLevel, int patientCount, Instant createdAt) {}
record RegulatoryReport(UUID id, String reportType, LocalDate periodStart, LocalDate periodEnd, String status, Instant createdAt) {}

record HealthTrendRequest(@NotBlank String metric, @NotBlank String segment, LocalDate periodStart, LocalDate periodEnd, @Min(0) int value) {}
record ChronicDiseaseCohortRequest(@NotBlank String condition, @NotBlank String riskLevel, @Min(0) int patientCount) {}
record RegulatoryReportRequest(@NotBlank String reportType, LocalDate periodStart, LocalDate periodEnd) {}
