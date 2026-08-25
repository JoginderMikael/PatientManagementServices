package git.jogindermikael.inventorypharmacyservice.controller;

import git.jogindermikael.inventorypharmacyservice.dto.InventoryPharmacyDtos.*;
import git.jogindermikael.inventorypharmacyservice.model.InventoryPharmacyModels.*;
import git.jogindermikael.inventorypharmacyservice.service.InventoryPharmacyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory-pharmacy")
@Tag(name = "Inventory & Pharmacy", description = "Medical supplies, medication stock and pharmacy prescription fulfillment")
public class InventoryPharmacyController {
    private final InventoryPharmacyService inventoryPharmacyService;

    public InventoryPharmacyController(InventoryPharmacyService inventoryPharmacyService) {
        this.inventoryPharmacyService = inventoryPharmacyService;
    }

    @PostMapping("/supplies")
    @Operation(summary = "Track medical supply stock")
    public ResponseEntity<SupplyItem> upsertSupply(@Valid @RequestBody SupplyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryPharmacyService.upsertSupply(request));
    }

    @GetMapping("/supplies")
    @Operation(summary = "List medical supplies")
    public List<SupplyItem> listSupplies() {
        return inventoryPharmacyService.listSupplies();
    }

    @PostMapping("/medications")
    @Operation(summary = "Track medication stock")
    public ResponseEntity<MedicationStock> upsertMedication(@Valid @RequestBody MedicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryPharmacyService.upsertMedication(request));
    }

    @GetMapping("/medications")
    @Operation(summary = "List medication stock")
    public List<MedicationStock> listMedications() {
        return inventoryPharmacyService.listMedications();
    }

    @PostMapping("/prescriptions")
    @Operation(summary = "Receive a prescription from EHR for pharmacy fulfillment")
    public ResponseEntity<PharmacyPrescription> receivePrescription(@Valid @RequestBody PharmacyPrescriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryPharmacyService.receivePrescription(request));
    }

    @PostMapping("/prescriptions/{id}/dispense")
    @Operation(summary = "Dispense a pharmacy prescription")
    public PharmacyPrescription dispense(@PathVariable UUID id) {
        return inventoryPharmacyService.dispense(id);
    }

    @PostMapping("/medication-charges")
    @Operation(summary = "Create a medication charge for billing")
    public ResponseEntity<MedicationCharge> createCharge(@Valid @RequestBody MedicationChargeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryPharmacyService.createCharge(request));
    }

    @GetMapping("/medication-charges")
    @Operation(summary = "List medication charges")
    public List<MedicationCharge> listCharges() {
        return inventoryPharmacyService.listCharges();
    }
}
