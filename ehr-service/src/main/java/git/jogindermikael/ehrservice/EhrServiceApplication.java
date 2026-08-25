package git.jogindermikael.ehrservice;

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
public class EhrServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EhrServiceApplication.class, args);
    }
}

@RestController
@RequestMapping("/ehr")
@Tag(name = "EHR", description = "Patient medical history, diagnoses, prescriptions, lab results and vaccination records")
class EhrController {
    private final Map<UUID, MedicalHistory> histories = new ConcurrentHashMap<>();
    private final Map<UUID, Diagnosis> diagnoses = new ConcurrentHashMap<>();
    private final Map<UUID, Prescription> prescriptions = new ConcurrentHashMap<>();
    private final Map<UUID, LabResult> labResults = new ConcurrentHashMap<>();
    private final Map<UUID, VaccinationRecord> vaccinations = new ConcurrentHashMap<>();

    @PostMapping("/histories")
    @Operation(summary = "Record patient medical history")
    ResponseEntity<MedicalHistory> recordHistory(@Valid @RequestBody MedicalHistoryRequest request) {
        UUID id = UUID.randomUUID();
        MedicalHistory history = new MedicalHistory(id, request.patientId(), request.summary(), request.allergies(), request.chronicConditions(), Instant.now());
        histories.put(id, history);
        return ResponseEntity.status(HttpStatus.CREATED).body(history);
    }

    @GetMapping("/histories/{patientId}")
    @Operation(summary = "List patient medical histories")
    List<MedicalHistory> historiesForPatient(@PathVariable UUID patientId) {
        return histories.values().stream().filter(item -> item.patientId().equals(patientId)).toList();
    }

    @PostMapping("/diagnoses")
    @Operation(summary = "Record a patient diagnosis")
    ResponseEntity<Diagnosis> recordDiagnosis(@Valid @RequestBody DiagnosisRequest request) {
        UUID id = UUID.randomUUID();
        Diagnosis diagnosis = new Diagnosis(id, request.patientId(), request.clinicianId(), request.code(), request.description(), request.diagnosedOn(), Instant.now());
        diagnoses.put(id, diagnosis);
        return ResponseEntity.status(HttpStatus.CREATED).body(diagnosis);
    }

    @GetMapping("/diagnoses/{patientId}")
    @Operation(summary = "List patient diagnoses")
    List<Diagnosis> diagnosesForPatient(@PathVariable UUID patientId) {
        return diagnoses.values().stream().filter(item -> item.patientId().equals(patientId)).toList();
    }

    @PostMapping("/prescriptions")
    @Operation(summary = "Create a prescription")
    ResponseEntity<Prescription> prescribe(@Valid @RequestBody PrescriptionRequest request) {
        UUID id = UUID.randomUUID();
        Prescription prescription = new Prescription(id, request.patientId(), request.clinicianId(), request.medication(), request.dosage(), request.instructions(), "ACTIVE", Instant.now());
        prescriptions.put(id, prescription);
        return ResponseEntity.status(HttpStatus.CREATED).body(prescription);
    }

    @GetMapping("/prescriptions/{patientId}")
    @Operation(summary = "List patient prescriptions")
    List<Prescription> prescriptionsForPatient(@PathVariable UUID patientId) {
        return prescriptions.values().stream().filter(item -> item.patientId().equals(patientId)).toList();
    }

    @PostMapping("/lab-results")
    @Operation(summary = "Store a lab result")
    ResponseEntity<LabResult> addLabResult(@Valid @RequestBody LabResultRequest request) {
        UUID id = UUID.randomUUID();
        LabResult result = new LabResult(id, request.patientId(), request.testName(), request.resultSummary(), request.source(), request.collectedOn(), Instant.now());
        labResults.put(id, result);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/lab-results/import")
    @Operation(summary = "Import a lab result from an external LIS payload")
    ResponseEntity<LabResult> importExternalLabResult(@Valid @RequestBody ExternalLabResultRequest request) {
        UUID id = UUID.randomUUID();
        LabResult result = new LabResult(id, request.patientId(), request.testName(), request.resultSummary(), "LIS:" + request.externalSystem(), request.collectedOn(), Instant.now());
        labResults.put(id, result);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }

    @GetMapping("/lab-results/{patientId}")
    @Operation(summary = "List patient lab results")
    List<LabResult> labResultsForPatient(@PathVariable UUID patientId) {
        return labResults.values().stream().filter(item -> item.patientId().equals(patientId)).toList();
    }

    @PostMapping("/vaccinations")
    @Operation(summary = "Record a vaccination")
    ResponseEntity<VaccinationRecord> recordVaccination(@Valid @RequestBody VaccinationRequest request) {
        UUID id = UUID.randomUUID();
        VaccinationRecord vaccination = new VaccinationRecord(id, request.patientId(), request.vaccine(), request.administeredOn(), request.lotNumber(), Instant.now());
        vaccinations.put(id, vaccination);
        return ResponseEntity.status(HttpStatus.CREATED).body(vaccination);
    }

    @GetMapping("/vaccinations/{patientId}")
    @Operation(summary = "List patient vaccination records")
    List<VaccinationRecord> vaccinationsForPatient(@PathVariable UUID patientId) {
        return vaccinations.values().stream().filter(item -> item.patientId().equals(patientId)).toList();
    }
}

record MedicalHistory(UUID id, UUID patientId, String summary, List<String> allergies, List<String> chronicConditions, Instant createdAt) {}
record Diagnosis(UUID id, UUID patientId, UUID clinicianId, String code, String description, LocalDate diagnosedOn, Instant createdAt) {}
record Prescription(UUID id, UUID patientId, UUID clinicianId, String medication, String dosage, String instructions, String status, Instant createdAt) {}
record LabResult(UUID id, UUID patientId, String testName, String resultSummary, String source, LocalDate collectedOn, Instant importedAt) {}
record VaccinationRecord(UUID id, UUID patientId, String vaccine, LocalDate administeredOn, String lotNumber, Instant createdAt) {}

record MedicalHistoryRequest(@NotNull UUID patientId, @NotBlank String summary, List<String> allergies, List<String> chronicConditions) {}
record DiagnosisRequest(@NotNull UUID patientId, @NotNull UUID clinicianId, @NotBlank String code, @NotBlank String description, @NotNull LocalDate diagnosedOn) {}
record PrescriptionRequest(@NotNull UUID patientId, @NotNull UUID clinicianId, @NotBlank String medication, @NotBlank String dosage, @NotBlank String instructions) {}
record LabResultRequest(@NotNull UUID patientId, @NotBlank String testName, @NotBlank String resultSummary, @NotBlank String source, @NotNull LocalDate collectedOn) {}
record ExternalLabResultRequest(@NotNull UUID patientId, @NotBlank String externalSystem, @NotBlank String testName, @NotBlank String resultSummary, @NotNull LocalDate collectedOn) {}
record VaccinationRequest(@NotNull UUID patientId, @NotBlank String vaccine, @NotNull LocalDate administeredOn, @NotBlank String lotNumber) {}
