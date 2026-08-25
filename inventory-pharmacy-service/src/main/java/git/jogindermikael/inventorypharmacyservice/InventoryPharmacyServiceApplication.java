package git.jogindermikael.inventorypharmacyservice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class InventoryPharmacyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryPharmacyServiceApplication.class, args);
    }
}

@RestController
@RequestMapping("/inventory-pharmacy")
@Tag(name = "Inventory & Pharmacy", description = "Medical supplies, medication stock and pharmacy prescription fulfillment")
class InventoryPharmacyController {
    private final Map<UUID, SupplyItem> supplies = new ConcurrentHashMap<>();
    private final Map<UUID, MedicationStock> medications = new ConcurrentHashMap<>();
    private final Map<UUID, PharmacyPrescription> prescriptions = new ConcurrentHashMap<>();
    private final Map<UUID, MedicationCharge> charges = new ConcurrentHashMap<>();

    @PostMapping("/supplies")
    @Operation(summary = "Track medical supply stock")
    ResponseEntity<SupplyItem> upsertSupply(@Valid @RequestBody SupplyRequest request) {
        UUID id = request.id() == null ? UUID.randomUUID() : request.id();
        SupplyItem item = new SupplyItem(id, request.name(), request.quantityOnHand(), request.reorderThreshold(), request.unitCost(), Instant.now());
        supplies.put(id, item);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @GetMapping("/supplies")
    @Operation(summary = "List medical supplies")
    List<SupplyItem> listSupplies() {
        return supplies.values().stream().sorted(Comparator.comparing(SupplyItem::name)).toList();
    }

    @PostMapping("/medications")
    @Operation(summary = "Track medication stock")
    ResponseEntity<MedicationStock> upsertMedication(@Valid @RequestBody MedicationRequest request) {
        UUID id = request.id() == null ? UUID.randomUUID() : request.id();
        MedicationStock medication = new MedicationStock(id, request.name(), request.ndcCode(), request.quantityOnHand(), request.unitCost(), Instant.now());
        medications.put(id, medication);
        return ResponseEntity.status(HttpStatus.CREATED).body(medication);
    }

    @GetMapping("/medications")
    @Operation(summary = "List medication stock")
    List<MedicationStock> listMedications() {
        return medications.values().stream().sorted(Comparator.comparing(MedicationStock::name)).toList();
    }

    @PostMapping("/prescriptions")
    @Operation(summary = "Receive a prescription from EHR for pharmacy fulfillment")
    ResponseEntity<PharmacyPrescription> receivePrescription(@Valid @RequestBody PharmacyPrescriptionRequest request) {
        UUID id = UUID.randomUUID();
        PharmacyPrescription prescription = new PharmacyPrescription(id, request.ehrPrescriptionId(), request.patientId(), request.medication(), request.quantity(), "READY_FOR_REVIEW", Instant.now());
        prescriptions.put(id, prescription);
        return ResponseEntity.status(HttpStatus.CREATED).body(prescription);
    }

    @PostMapping("/prescriptions/{id}/dispense")
    @Operation(summary = "Dispense a pharmacy prescription")
    PharmacyPrescription dispense(@PathVariable UUID id) {
        PharmacyPrescription prescription = Optional.ofNullable(prescriptions.get(id)).orElseThrow();
        PharmacyPrescription dispensed = new PharmacyPrescription(prescription.id(), prescription.ehrPrescriptionId(), prescription.patientId(), prescription.medication(), prescription.quantity(), "DISPENSED", Instant.now());
        prescriptions.put(id, dispensed);
        return dispensed;
    }

    @PostMapping("/medication-charges")
    @Operation(summary = "Create a medication charge for billing")
    ResponseEntity<MedicationCharge> createCharge(@Valid @RequestBody MedicationChargeRequest request) {
        UUID id = UUID.randomUUID();
        MedicationCharge charge = new MedicationCharge(id, request.patientId(), request.prescriptionId(), request.amount(), "PENDING_BILLING", Instant.now());
        charges.put(id, charge);
        return ResponseEntity.status(HttpStatus.CREATED).body(charge);
    }

    @GetMapping("/medication-charges")
    @Operation(summary = "List medication charges")
    List<MedicationCharge> listCharges() {
        return charges.values().stream().sorted(Comparator.comparing(MedicationCharge::createdAt)).toList();
    }
}

record SupplyItem(UUID id, String name, int quantityOnHand, int reorderThreshold, BigDecimal unitCost, Instant updatedAt) {}
record MedicationStock(UUID id, String name, String ndcCode, int quantityOnHand, BigDecimal unitCost, Instant updatedAt) {}
record PharmacyPrescription(UUID id, UUID ehrPrescriptionId, UUID patientId, String medication, int quantity, String status, Instant updatedAt) {}
record MedicationCharge(UUID id, UUID patientId, UUID prescriptionId, BigDecimal amount, String status, Instant createdAt) {}

record SupplyRequest(UUID id, @NotBlank String name, @Min(0) int quantityOnHand, @Min(0) int reorderThreshold, @NotNull @DecimalMin("0.00") BigDecimal unitCost) {}
record MedicationRequest(UUID id, @NotBlank String name, @NotBlank String ndcCode, @Min(0) int quantityOnHand, @NotNull @DecimalMin("0.00") BigDecimal unitCost) {}
record PharmacyPrescriptionRequest(@NotNull UUID ehrPrescriptionId, @NotNull UUID patientId, @NotBlank String medication, @Min(1) int quantity) {}
record MedicationChargeRequest(@NotNull UUID patientId, @NotNull UUID prescriptionId, @NotNull @DecimalMin("0.00") BigDecimal amount) {}
