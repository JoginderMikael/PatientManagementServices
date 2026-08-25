package git.jogindermikael.inventorypharmacyservice.service;

import git.jogindermikael.inventorypharmacyservice.dto.InventoryPharmacyDtos.*;
import git.jogindermikael.inventorypharmacyservice.mapper.InventoryPharmacyMapper;
import git.jogindermikael.inventorypharmacyservice.model.InventoryPharmacyModels.*;
import git.jogindermikael.inventorypharmacyservice.repository.InventoryPharmacyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryPharmacyService {
    private final InventoryPharmacyRepository repository;
    private final InventoryPharmacyMapper mapper;

    public InventoryPharmacyService(InventoryPharmacyRepository repository, InventoryPharmacyMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public SupplyItem upsertSupply(SupplyRequest request) { return repository.saveSupply(mapper.toSupply(request)); }
    public List<SupplyItem> listSupplies() { return repository.findSupplies().stream().sorted(Comparator.comparing(SupplyItem::name)).toList(); }
    public MedicationStock upsertMedication(MedicationRequest request) { return repository.saveMedication(mapper.toMedication(request)); }
    public List<MedicationStock> listMedications() { return repository.findMedications().stream().sorted(Comparator.comparing(MedicationStock::name)).toList(); }
    public PharmacyPrescription receivePrescription(PharmacyPrescriptionRequest request) { return repository.savePrescription(mapper.toPrescription(request)); }

    public PharmacyPrescription dispense(UUID id) {
        PharmacyPrescription prescription = repository.findPrescriptionById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prescription not found"));
        return repository.savePrescription(mapper.toDispensedPrescription(prescription));
    }

    public MedicationCharge createCharge(MedicationChargeRequest request) { return repository.saveCharge(mapper.toCharge(request)); }
    public List<MedicationCharge> listCharges() { return repository.findCharges().stream().sorted(Comparator.comparing(MedicationCharge::createdAt)).toList(); }
}
