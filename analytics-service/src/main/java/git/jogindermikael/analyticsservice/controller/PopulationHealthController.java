package git.jogindermikael.analyticsservice.controller;

import git.jogindermikael.analyticsservice.dto.PopulationHealthDtos.*;
import git.jogindermikael.analyticsservice.model.PopulationHealthModels.*;
import git.jogindermikael.analyticsservice.service.PopulationHealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
@RequestMapping("/analytics")
@Tag(name = "Reporting & Population Health", description = "Health trends, chronic disease cohorts and regulatory reporting")
public class PopulationHealthController {
    private final PopulationHealthService populationHealthService;

    public PopulationHealthController(PopulationHealthService populationHealthService) {
        this.populationHealthService = populationHealthService;
    }

    @PostMapping("/health-trends")
    @Operation(summary = "Record a population health trend")
    public ResponseEntity<HealthTrend> recordTrend(@Valid @RequestBody HealthTrendRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(populationHealthService.recordTrend(request));
    }

    @GetMapping("/health-trends")
    @Operation(summary = "List population health trends")
    public List<HealthTrend> listTrends() {
        return populationHealthService.listTrends();
    }

    @PostMapping("/chronic-disease-cohorts")
    @Operation(summary = "Create a chronic disease cohort")
    public ResponseEntity<ChronicDiseaseCohort> createCohort(@Valid @RequestBody ChronicDiseaseCohortRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(populationHealthService.createCohort(request));
    }

    @GetMapping("/chronic-disease-cohorts")
    @Operation(summary = "List chronic disease cohorts")
    public List<ChronicDiseaseCohort> listCohorts() {
        return populationHealthService.listCohorts();
    }

    @PostMapping("/regulatory-reports")
    @Operation(summary = "Generate a regulatory report")
    public ResponseEntity<RegulatoryReport> generateReport(@Valid @RequestBody RegulatoryReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(populationHealthService.generateReport(request));
    }

    @GetMapping("/regulatory-reports")
    @Operation(summary = "List regulatory reports")
    public List<RegulatoryReport> listReports() {
        return populationHealthService.listReports();
    }
}
