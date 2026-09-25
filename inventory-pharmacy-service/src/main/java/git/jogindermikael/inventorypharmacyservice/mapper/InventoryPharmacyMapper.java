package git.jogindermikael.inventorypharmacyservice.mapper;

import git.jogindermikael.inventorypharmacyservice.dto.*;
import git.jogindermikael.inventorypharmacyservice.model.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class InventoryPharmacyMapper {
    public SupplyItem toSupply(SupplyRequest request) {
        return new SupplyItem(request.id() == null ? UUID.randomUUID() : request.id(), request.name(), request.quantityOnHand(), request.reorderThreshold(), request.unitCost(), Instant.now());
    }

    public MedicationStock toMedication(MedicationRequest request) {
        return new MedicationStock(request.id() == null ? UUID.randomUUID() : request.id(), request.name(), request.ndcCode(), request.quantityOnHand(), request.unitCost(), Instant.now());
    }

    public PharmacyPrescription toPrescription(PharmacyPrescriptionRequest request) {
        return new PharmacyPrescription(UUID.randomUUID(), request.ehrPrescriptionId(), request.patientId(), request.medication(), request.quantity(), "READY_FOR_REVIEW", Instant.now());
    }

    public PharmacyPrescription toDispensedPrescription(PharmacyPrescription prescription) {
        return new PharmacyPrescription(prescription.id(), prescription.ehrPrescriptionId(), prescription.patientId(), prescription.medication(), prescription.quantity(), "DISPENSED", Instant.now());
    }

    public MedicationCharge toCharge(MedicationChargeRequest request) {
        return new MedicationCharge(UUID.randomUUID(), request.patientId(), request.prescriptionId(), request.amount(), "PENDING_BILLING", Instant.now());
    }
}
