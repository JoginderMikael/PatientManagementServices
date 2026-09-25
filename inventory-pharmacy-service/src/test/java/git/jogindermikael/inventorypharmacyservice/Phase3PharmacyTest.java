package git.jogindermikael.inventorypharmacyservice;

import static org.junit.jupiter.api.Assertions.*;

import git.jogindermikael.inventorypharmacyservice.dto.*;
import git.jogindermikael.inventorypharmacyservice.service.InventoryPharmacyService;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(properties = {"app.audit.enabled=false", "grpc.server.port=-1"})
class Phase3PharmacyTest {

  @Autowired InventoryPharmacyService service;

  @org.springframework.boot.test.context.TestConfiguration
  static class SafetyFixture {
    @org.springframework.context.annotation.Bean
    @org.springframework.context.annotation.Primary
    git.jogindermikael.inventorypharmacyservice.integration.PrescriptionSafetyClient safety() {
      return new git.jogindermikael.inventorypharmacyservice.integration.PrescriptionSafetyClient(
          "http://localhost:1") {
        @Override
        public void verify(UUID prescription, UUID patient, String medication) {}
      };
    }
  }

  @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;

  @Test
  @WithMockUser(roles = "PHARMACIST")
  void dispensingRequiresReviewAndMovesStockOnce() {
    var med =
        service.upsertMedication(
            new MedicationRequest(
                null, "Synthetic-A", UUID.randomUUID().toString(), 0, new BigDecimal("2.00")));
    var receipt =
        new BatchReceipt(
            med.id(),
            UUID.randomUUID().toString(),
            LocalDate.now().plusDays(30),
            10,
            UUID.randomUUID().toString());
    service.receive(receipt);
    service.receive(receipt);
    var command =
        new PharmacyPrescriptionRequest(UUID.randomUUID(), UUID.randomUUID(), "Synthetic-A", 3);
    var rx = service.receivePrescription(command);
    assertEquals(rx.id(), service.receivePrescription(command).id());
    assertThrows(ResponseStatusException.class, () -> service.dispense(rx.id()));
    service.review(
        rx.id(),
        new SafetyReview(
            med.id(), true, true, true, "Verified synthetic order"));
    assertEquals("DISPENSED", service.dispense(rx.id()).status());
    service.dispense(rx.id());
    assertEquals(
        7,
        service.listMedications().stream()
            .filter(v -> v.id().equals(med.id()))
            .findFirst()
            .orElseThrow()
            .quantityOnHand());
    assertEquals(2, service.movements(med.id()).size());
    assertEquals(
        new BigDecimal("6.00"),
        service.listCharges().stream()
            .filter(v -> v.prescriptionId().equals(rx.id()))
            .findFirst()
            .orElseThrow()
            .amount());
  }

  @Test
  @WithMockUser(roles = "PHARMACIST")
  void expiredStockCannotBeDispensed() {
    var med =
        service.upsertMedication(
            new MedicationRequest(
                null, "Synthetic-B", UUID.randomUUID().toString(), 0, BigDecimal.ONE));
    service.receive(
        new BatchReceipt(
            med.id(), "lot", LocalDate.now().plusDays(1), 5, UUID.randomUUID().toString()));
    jdbc.update(
        "UPDATE medication_batch SET expires_on=? WHERE medication_id=?",
        LocalDate.now().minusDays(1),
        med.id());
    var rx =
        service.receivePrescription(
            new PharmacyPrescriptionRequest(
                UUID.randomUUID(), UUID.randomUUID(), "Synthetic-B", 1));
    service.review(
        rx.id(), new SafetyReview(med.id(), true, true, true, "Reviewed"));
    assertThrows(ResponseStatusException.class, () -> service.dispense(rx.id()));
    assertEquals(1, service.movements(med.id()).size());
  }

  @Test
  @WithMockUser(roles = "PHARMACIST")
  void competingDispensesCannotOversell() throws Exception {
    var med =
        service.upsertMedication(
            new MedicationRequest(
                null, "Synthetic-C", UUID.randomUUID().toString(), 0, BigDecimal.ONE));
    service.receive(
        new BatchReceipt(
            med.id(), "lot", LocalDate.now().plusDays(1), 1, UUID.randomUUID().toString()));
    var ids = new ArrayList<UUID>();
    for (int i = 0; i < 2; i++) {
      var rx =
          service.receivePrescription(
              new PharmacyPrescriptionRequest(
                  UUID.randomUUID(), UUID.randomUUID(), "Synthetic-C", 1));
      service.review(
          rx.id(),
          new SafetyReview(med.id(), true, true, true, "Reviewed"));
      ids.add(rx.id());
    }
    try (var pool = java.util.concurrent.Executors.newFixedThreadPool(2)) {
      var jobs =
          ids.stream()
              .<java.util.concurrent.Callable<Boolean>>map(
                  id ->
                      () -> {
                        try {
                          service.dispense(id);
                          return true;
                        } catch (ResponseStatusException e) {
                          return false;
                        }
                      })
              .toList();
      int successes = 0;
      for (var f : pool.invokeAll(jobs)) if (f.get()) successes++;
      assertEquals(1, successes);
    }
    assertEquals(
        0,
        service.listMedications().stream()
            .filter(v -> v.id().equals(med.id()))
            .findFirst()
            .orElseThrow()
            .quantityOnHand());
  }
}
