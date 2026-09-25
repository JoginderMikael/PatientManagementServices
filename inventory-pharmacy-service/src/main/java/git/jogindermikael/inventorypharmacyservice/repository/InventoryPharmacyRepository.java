package git.jogindermikael.inventorypharmacyservice.repository;

import git.jogindermikael.inventorypharmacyservice.model.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryPharmacyRepository {
  private final JdbcTemplate jdbc;

  public InventoryPharmacyRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public void lock() {
    jdbc.queryForObject("SELECT id FROM workflow_lock WHERE id=1 FOR UPDATE", Integer.class);
  }

  private SupplyItem save(SupplyItem v) {
    if (jdbc.update(
            "UPDATE supply_item SET name=?, quantity_on_hand=?, reorder_threshold=?, unit_cost=?,"
                + " updated_at=? WHERE id=?",
            v.name(),
            v.quantityOnHand(),
            v.reorderThreshold(),
            v.unitCost(),
            v.updatedAt().atOffset(ZoneOffset.UTC),
            v.id())
        == 0)
      jdbc.update(
          "INSERT INTO supply_item VALUES (?,?,?,?,?,?)",
          v.id(),
          v.name(),
          v.quantityOnHand(),
          v.reorderThreshold(),
          v.unitCost(),
          v.updatedAt().atOffset(ZoneOffset.UTC));
    return v;
  }

  private List<SupplyItem> supplies(String where, Object... args) {
    return jdbc.query(
        "SELECT * FROM supply_item " + where + " ORDER BY id",
        (rs, row) ->
            new SupplyItem(
                rs.getObject("id", UUID.class),
                rs.getString("name"),
                rs.getInt("quantity_on_hand"),
                rs.getInt("reorder_threshold"),
                rs.getBigDecimal("unit_cost"),
                rs.getObject("updated_at", java.time.OffsetDateTime.class).toInstant()),
        args);
  }

  private MedicationStock save(MedicationStock v) {
    if (jdbc.update(
            "UPDATE medication_stock SET name=?, ndc_code=?, quantity_on_hand=?, unit_cost=?,"
                + " updated_at=? WHERE id=?",
            v.name(),
            v.ndcCode(),
            v.quantityOnHand(),
            v.unitCost(),
            v.updatedAt().atOffset(ZoneOffset.UTC),
            v.id())
        == 0)
      jdbc.update(
          "INSERT INTO medication_stock VALUES (?,?,?,?,?,?)",
          v.id(),
          v.name(),
          v.ndcCode(),
          v.quantityOnHand(),
          v.unitCost(),
          v.updatedAt().atOffset(ZoneOffset.UTC));
    return v;
  }

  private List<MedicationStock> medications(String where, Object... args) {
    return jdbc.query(
        "SELECT * FROM medication_stock " + where + " ORDER BY id",
        (rs, row) ->
            new MedicationStock(
                rs.getObject("id", UUID.class),
                rs.getString("name"),
                rs.getString("ndc_code"),
                rs.getInt("quantity_on_hand"),
                rs.getBigDecimal("unit_cost"),
                rs.getObject("updated_at", java.time.OffsetDateTime.class).toInstant()),
        args);
  }

  private PharmacyPrescription save(PharmacyPrescription v) {
    if (jdbc.update(
            "UPDATE pharmacy_prescription SET ehr_prescription_id=?, patient_id=?, medication=?,"
                + " quantity=?, status=?, updated_at=? WHERE id=?",
            v.ehrPrescriptionId(),
            v.patientId(),
            v.medication(),
            v.quantity(),
            v.status(),
            v.updatedAt().atOffset(ZoneOffset.UTC),
            v.id())
        == 0)
      jdbc.update(
          "INSERT INTO pharmacy_prescription VALUES (?,?,?,?,?,?,?)",
          v.id(),
          v.ehrPrescriptionId(),
          v.patientId(),
          v.medication(),
          v.quantity(),
          v.status(),
          v.updatedAt().atOffset(ZoneOffset.UTC));
    return v;
  }

  private List<PharmacyPrescription> prescriptions(String where, Object... args) {
    return jdbc.query(
        "SELECT * FROM pharmacy_prescription " + where + " ORDER BY id",
        (rs, row) ->
            new PharmacyPrescription(
                rs.getObject("id", UUID.class),
                rs.getObject("ehr_prescription_id", UUID.class),
                rs.getObject("patient_id", UUID.class),
                rs.getString("medication"),
                rs.getInt("quantity"),
                rs.getString("status"),
                rs.getObject("updated_at", java.time.OffsetDateTime.class).toInstant()),
        args);
  }

  private MedicationCharge save(MedicationCharge v) {
    if (jdbc.update(
            "UPDATE medication_charge SET patient_id=?, prescription_id=?, amount=?, status=?,"
                + " created_at=? WHERE id=?",
            v.patientId(),
            v.prescriptionId(),
            v.amount(),
            v.status(),
            v.createdAt().atOffset(ZoneOffset.UTC),
            v.id())
        == 0)
      jdbc.update(
          "INSERT INTO medication_charge VALUES (?,?,?,?,?,?)",
          v.id(),
          v.patientId(),
          v.prescriptionId(),
          v.amount(),
          v.status(),
          v.createdAt().atOffset(ZoneOffset.UTC));
    return v;
  }

  private List<MedicationCharge> charges(String where, Object... args) {
    return jdbc.query(
        "SELECT * FROM medication_charge " + where + " ORDER BY id",
        (rs, row) ->
            new MedicationCharge(
                rs.getObject("id", UUID.class),
                rs.getObject("patient_id", UUID.class),
                rs.getObject("prescription_id", UUID.class),
                rs.getBigDecimal("amount"),
                rs.getString("status"),
                rs.getObject("created_at", java.time.OffsetDateTime.class).toInstant()),
        args);
  }

  public SupplyItem saveSupply(SupplyItem item) {
    return save(item);
  }

  public Collection<SupplyItem> findSupplies() {
    return supplies("");
  }

  public MedicationStock saveMedication(MedicationStock medication) {
    return save(medication);
  }

  public Collection<MedicationStock> findMedications() {
    return medications("");
  }

  public PharmacyPrescription savePrescription(PharmacyPrescription prescription) {
    return save(prescription);
  }

  public Optional<PharmacyPrescription> findPrescriptionById(UUID id) {
    return prescriptions("WHERE id=?", id).stream().findFirst();
  }

  public MedicationCharge saveCharge(MedicationCharge charge) {
    return save(charge);
  }

  public Collection<MedicationCharge> findCharges() {
    return charges("");
  }

  public Collection<PharmacyPrescription> prescriptionsForPatient(UUID id) {
    return prescriptions("WHERE patient_id=?", id);
  }

  public Collection<MedicationCharge> chargesForPatient(UUID id) {
    return charges("WHERE patient_id=?", id);
  }
}
