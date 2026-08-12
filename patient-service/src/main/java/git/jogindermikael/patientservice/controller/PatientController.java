package git.jogindermikael.patientservice.controller;

import git.jogindermikael.patientservice.dto.PatientRequestDTO;
import git.jogindermikael.patientservice.dto.PatientResponseDTO;
import git.jogindermikael.patientservice.dto.validators.CreatePatientValidationGroup;
import git.jogindermikael.patientservice.model.Patient;
import git.jogindermikael.patientservice.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
@Tag(name = "Patient", description = "API for Managing Patients")
public class PatientController {

    private final PatientService patientService;


    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping()
    @Operation(summary = "Get patients")
    public ResponseEntity<List<PatientResponseDTO>> getPatients() {
        return ResponseEntity.ok().body(patientService.getPatients());
    }

    @PostMapping
    @Operation(summary = "Create a new patient")
    public ResponseEntity<PatientResponseDTO> createPatient(@Validated({Default.class, CreatePatientValidationGroup.class}) @RequestBody PatientRequestDTO patientRequestDTO) {
        return ResponseEntity.ok().body(patientService.createPatient(patientRequestDTO));
    }


    @PutMapping("/{id}")
    @Operation(summary = "Update a patient")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable UUID id, @Validated({Default.class}) @RequestBody PatientRequestDTO patientRequestDTO) {
        PatientResponseDTO patientResponseDTO = patientService.updatePatient(id, patientRequestDTO);
        return ResponseEntity.ok().body(patientResponseDTO);

    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete patient")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
