package git.jogindermikael.inventorypharmacyservice.repository;

import git.jogindermikael.inventorypharmacyservice.model.InventoryPharmacyModels.*;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InventoryPharmacyRepository {
    private final ConcurrentHashMap<UUID, SupplyItem> supplies = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, MedicationStock> medications = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, PharmacyPrescription> prescriptions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, MedicationCharge> charges = new ConcurrentHashMap<>();

    public SupplyItem saveSupply(SupplyItem item) { supplies.put(item.id(), item); return item; }
    public Collection<SupplyItem> findSupplies() { return supplies.values(); }
    public MedicationStock saveMedication(MedicationStock medication) { medications.put(medication.id(), medication); return medication; }
    public Collection<MedicationStock> findMedications() { return medications.values(); }
    public PharmacyPrescription savePrescription(PharmacyPrescription prescription) { prescriptions.put(prescription.id(), prescription); return prescription; }
    public Optional<PharmacyPrescription> findPrescriptionById(UUID id) { return Optional.ofNullable(prescriptions.get(id)); }
    public MedicationCharge saveCharge(MedicationCharge charge) { charges.put(charge.id(), charge); return charge; }
    public Collection<MedicationCharge> findCharges() { return charges.values(); }
}
