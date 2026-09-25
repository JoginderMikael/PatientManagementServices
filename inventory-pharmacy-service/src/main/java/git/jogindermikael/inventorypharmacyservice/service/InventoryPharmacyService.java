package git.jogindermikael.inventorypharmacyservice.service;

import git.jogindermikael.inventorypharmacyservice.dto.*;
import git.jogindermikael.inventorypharmacyservice.mapper.InventoryPharmacyMapper;
import git.jogindermikael.inventorypharmacyservice.model.*;
import git.jogindermikael.inventorypharmacyservice.repository.InventoryPharmacyRepository;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.transaction.annotation.Transactional
@Service
public class InventoryPharmacyService {
  private final InventoryPharmacyRepository repository;
  private final InventoryPharmacyMapper mapper;
  private final git.jogindermikael.inventorypharmacyservice.integration.PrescriptionSafetyClient
      safety;
  private final org.springframework.jdbc.core.JdbcTemplate jdbc;

  public InventoryPharmacyService(
      InventoryPharmacyRepository repository,
      InventoryPharmacyMapper mapper,
      org.springframework.jdbc.core.JdbcTemplate jdbc,
      git.jogindermikael.inventorypharmacyservice.integration.PrescriptionSafetyClient safety) {
    this.safety = safety;
    this.jdbc = jdbc;
    this.repository = repository;
    this.mapper = mapper;
  }

  public SupplyItem upsertSupply(SupplyRequest request) {
    repository.lock();
    int before =
        request.id() == null
            ? 0
            : repository.findSupplies().stream()
                .filter(v -> v.id().equals(request.id()))
                .mapToInt(SupplyItem::quantityOnHand)
                .findFirst()
                .orElse(0);
    var item = repository.saveSupply(mapper.toSupply(request));
    if (item.quantityOnHand() != before)
      jdbc.update(
          "INSERT INTO supply_movement VALUES (?,?,?,CURRENT_TIMESTAMP)",
          UUID.randomUUID(),
          item.id(),
          item.quantityOnHand() - before);
    return item;
  }

  public List<SupplyItem> listSupplies() {
    return repository.findSupplies().stream()
        .sorted(Comparator.comparing(SupplyItem::name))
        .toList();
  }

  public MedicationStock upsertMedication(MedicationRequest request) {
    repository.lock();
    if (request.quantityOnHand() != 0)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receive stock through batches");
    if (request.id() != null
        && repository.findMedications().stream().anyMatch(v -> v.id().equals(request.id())))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Use stock movements to change inventory");
    return repository.saveMedication(mapper.toMedication(request));
  }

  public List<MedicationStock> listMedications() {
    return repository.findMedications().stream()
        .sorted(Comparator.comparing(MedicationStock::name))
        .toList();
  }

  public PharmacyPrescription receivePrescription(PharmacyPrescriptionRequest request) {
    repository.lock();
    var old =
        jdbc.queryForList(
            "SELECT id FROM pharmacy_prescription WHERE ehr_prescription_id=?",
            request.ehrPrescriptionId());
    if (!old.isEmpty()) {
      var rx = repository.findPrescriptionById((UUID) old.getFirst().get("id")).orElseThrow();
      if (!rx.patientId().equals(request.patientId())
          || !rx.medication().equals(request.medication())
          || rx.quantity() != request.quantity())
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Prescription source reused");
      return rx;
    }
    return repository.savePrescription(mapper.toPrescription(request));
  }

  public PharmacyPrescription dispense(UUID id) {
    repository.lock();
    PharmacyPrescription prescription =
        repository
            .findPrescriptionById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prescription not found"));
    if ("DISPENSED".equals(prescription.status())) return prescription;
    var reviews =
        jdbc.queryForList(
            "SELECT * FROM dispensing_review WHERE prescription_id=? AND outcome='APPROVED'", id);
    if (reviews.isEmpty())
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Pharmacist safety approval required");
    safety.verify(
        prescription.ehrPrescriptionId(), prescription.patientId(), prescription.medication());
    UUID medicationId = (UUID) reviews.getFirst().get("medication_id");
    var batches =
        jdbc.queryForList(
            "SELECT * FROM medication_batch WHERE medication_id=? AND expires_on>CURRENT_DATE AND"
                + " quantity>0 ORDER BY expires_on,id FOR UPDATE",
            medicationId);
    int available =
        batches.stream().mapToInt(row -> ((Number) row.get("quantity")).intValue()).sum();
    if (available < prescription.quantity())
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient unexpired stock");
    int remaining = prescription.quantity();
    for (var batch : batches) {
      int quantity = Math.min(remaining, ((Number) batch.get("quantity")).intValue());
      if (quantity == 0) break;
      jdbc.update(
          "UPDATE medication_batch SET quantity=quantity-? WHERE id=?", quantity, batch.get("id"));
      jdbc.update(
          "INSERT INTO stock_movement VALUES (?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",
          UUID.randomUUID(),
          medicationId,
          batch.get("id"),
          id,
          -quantity,
          "DISPENSE",
          id + ":" + batch.get("id"));
      remaining -= quantity;
    }
    jdbc.update(
        "UPDATE medication_stock SET"
            + " quantity_on_hand=quantity_on_hand-?,updated_at=CURRENT_TIMESTAMP WHERE id=?",
        prescription.quantity(),
        medicationId);
    var medication =
        repository.findMedications().stream()
            .filter(v -> v.id().equals(medicationId))
            .findFirst()
            .orElseThrow();
    repository.saveCharge(
        new MedicationCharge(
            UUID.randomUUID(),
            prescription.patientId(),
            id,
            medication.unitCost().multiply(java.math.BigDecimal.valueOf(prescription.quantity())),
            "PENDING_BILLING",
            java.time.Instant.now()));
    return repository.savePrescription(mapper.toDispensedPrescription(prescription));
  }

  public MedicationCharge createCharge(MedicationChargeRequest request) {
    repository.lock();
    var charge =
        repository.findCharges().stream()
            .filter(v -> v.prescriptionId().equals(request.prescriptionId()))
            .findFirst()
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.CONFLICT, "Dispense before billing"));
    if (!charge.patientId().equals(request.patientId())
        || charge.amount().compareTo(request.amount()) != 0)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Charge differs from dispensed medication");
    return charge;
  }

  public List<MedicationCharge> listCharges() {
    return repository.findCharges().stream()
        .sorted(Comparator.comparing(MedicationCharge::createdAt))
        .toList();
  }

  public java.util.Map<String, Object> receive(BatchReceipt c) {
    repository.lock();
    var existing =
        jdbc.queryForList("SELECT * FROM stock_movement WHERE reference=?", c.reference());
    if (!existing.isEmpty()) {
      var row = existing.getFirst();
      var batch =
          jdbc.queryForMap("SELECT * FROM medication_batch WHERE id=?", row.get("batch_id"));
      if (!c.medicationId().equals(row.get("medication_id"))
          || c.quantity() != ((Number) row.get("delta")).intValue()
          || !c.lot().equals(batch.get("lot"))
          || !c.expiresOn().equals(((java.sql.Date) batch.get("expires_on")).toLocalDate()))
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Receipt reference reused");
      return row;
    }
    if (repository.findMedications().stream().noneMatch(v -> v.id().equals(c.medicationId())))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found");
    if (c.quantity() < 1 || !c.expiresOn().isAfter(java.time.LocalDate.now()))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid batch");
    UUID batch = UUID.randomUUID();
    jdbc.update(
        "INSERT INTO medication_batch VALUES (?,?,?,?,?)",
        batch,
        c.medicationId(),
        c.lot(),
        c.expiresOn(),
        c.quantity());
    jdbc.update(
        "UPDATE medication_stock SET"
            + " quantity_on_hand=quantity_on_hand+?,updated_at=CURRENT_TIMESTAMP WHERE id=?",
        c.quantity(),
        c.medicationId());
    jdbc.update(
        "INSERT INTO stock_movement VALUES (?,?,?,NULL,?,?,?,CURRENT_TIMESTAMP)",
        UUID.randomUUID(),
        c.medicationId(),
        batch,
        c.quantity(),
        "RECEIPT",
        c.reference());
    return jdbc.queryForMap("SELECT * FROM stock_movement WHERE reference=?", c.reference());
  }

  public void review(UUID id, SafetyReview c) {
    repository.lock();
    var rx =
        repository
            .findPrescriptionById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prescription not found"));
    if ("DISPENSED".equals(rx.status()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Already dispensed");
    var medication =
        repository.findMedications().stream()
            .filter(v -> v.id().equals(c.medicationId()))
            .findFirst()
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found"));
    if (!medication.name().equalsIgnoreCase(rx.medication()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Medication mismatch");
    var auth =
        org.springframework.security.core.context.SecurityContextHolder.getContext()
            .getAuthentication();
    if (auth == null)
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reviewer identity required");
    jdbc.update("DELETE FROM dispensing_review WHERE prescription_id=?", id);
    jdbc.update(
        "INSERT INTO dispensing_review VALUES (?,?,?,CURRENT_TIMESTAMP,?,?)",
        id,
        c.medicationId(),
        auth.getName(),
        c.allergiesChecked() && c.interactionsChecked() && c.doseChecked() ? "APPROVED" : "BLOCKED",
        c.reason());
  }

  public java.util.List<java.util.Map<String, Object>> movements(UUID id) {
    return jdbc.queryForList(
        "SELECT * FROM stock_movement WHERE medication_id=? ORDER BY occurred_at", id);
  }

  public java.util.List<java.util.Map<String, Object>> supplyMovements(UUID id) {
    return jdbc.queryForList(
        "SELECT * FROM supply_movement WHERE supply_id=? ORDER BY occurred_at", id);
  }
}
