package git.jogindermikael.ehrservice.controller;

import git.jogindermikael.ehrservice.dto.EhrDtos.*;
import git.jogindermikael.ehrservice.model.EhrModels.*;
import git.jogindermikael.ehrservice.service.EhrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ehr")
@Tag(name = "EHR", description = "Patient medical history, diagnoses, prescriptions, lab results and vaccination records")
public class EhrController {
    private final EhrService ehrService;

    public EhrController(EhrService ehrService) {
        this.ehrService = ehrService;
    }

    @PostMapping("/histories")
    @Operation(summary = "Record patient medical history")
    public ResponseEntity<MedicalHistory> recordHistory(@Valid @RequestBody MedicalHistoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ehrService.recordHistory(request));
    }

    @GetMapping("/histories/{patientId}")
    @Operation(summary = "List patient medical histories")
    public List<MedicalHistory> historiesForPatient(@PathVariable UUID patientId) {
        return ehrService.historiesForPatient(patientId);
    }

    @PostMapping("/diagnoses")
    @Operation(summary = "Record a patient diagnosis")
    public ResponseEntity<Diagnosis> recordDiagnosis(@Valid @RequestBody DiagnosisRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ehrService.recordDiagnosis(request));
    }

    @GetMapping("/diagnoses/{patientId}")
    @Operation(summary = "List patient diagnoses")
    public List<Diagnosis> diagnosesForPatient(@PathVariable UUID patientId) {
        return ehrService.diagnosesForPatient(patientId);
    }

    @PostMapping("/prescriptions")
    @Operation(summary = "Create a prescription")
    public ResponseEntity<Prescription> prescribe(@Valid @RequestBody PrescriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ehrService.prescribe(request));
    }

    @GetMapping("/prescriptions/{patientId}")
    @Operation(summary = "List patient prescriptions")
    public List<Prescription> prescriptionsForPatient(@PathVariable UUID patientId) {
        return ehrService.prescriptionsForPatient(patientId);
    }

    @PostMapping("/lab-results")
    @Operation(summary = "Store a lab result")
    public ResponseEntity<LabResult> addLabResult(@Valid @RequestBody LabResultRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ehrService.addLabResult(request));
    }

    @PostMapping("/lab-results/import")
    @Operation(summary = "Import a lab result from an external LIS payload")
    public ResponseEntity<LabResult> importExternalLabResult(@Valid @RequestBody ExternalLabResultRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ehrService.importExternalLabResult(request));
    }

    @GetMapping("/lab-results/{patientId}")
    @Operation(summary = "List patient lab results")
    public List<LabResult> labResultsForPatient(@PathVariable UUID patientId) {
        return ehrService.labResultsForPatient(patientId);
    }

    @PostMapping("/vaccinations")
    @Operation(summary = "Record a vaccination")
    public ResponseEntity<VaccinationRecord> recordVaccination(@Valid @RequestBody VaccinationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ehrService.recordVaccination(request));
    }

    @GetMapping("/vaccinations/{patientId}")
    @Operation(summary = "List patient vaccination records")
    public List<VaccinationRecord> vaccinationsForPatient(@PathVariable UUID patientId) {
        return ehrService.vaccinationsForPatient(patientId);
    }
}
