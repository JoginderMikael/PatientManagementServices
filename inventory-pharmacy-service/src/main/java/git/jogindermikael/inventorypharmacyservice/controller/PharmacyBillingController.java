package git.jogindermikael.inventorypharmacyservice.controller;

import git.jogindermikael.inventorypharmacyservice.integration.WorkflowClient;
import git.jogindermikael.inventorypharmacyservice.service.InventoryPharmacyService;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/inventory-pharmacy")
@PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
public class PharmacyBillingController {
  private final WorkflowClient client;
  private final InventoryPharmacyService service;

  public PharmacyBillingController(WorkflowClient client, InventoryPharmacyService service) {
    this.client = client;
    this.service = service;
  }

  public record Bill(
      @jakarta.validation.constraints.NotNull
          @jakarta.validation.constraints.Pattern(regexp = "[A-Z]{3}")
          String currency) {}

  @PostMapping("/prescriptions/{id}/bill")
  public Map<String, Object> bill(
      @PathVariable UUID id, @jakarta.validation.Valid @RequestBody Bill c) {
    var charge =
        service.listCharges().stream()
            .filter(v -> v.prescriptionId().equals(id))
            .findFirst()
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.CONFLICT, "No dispensed charge"));
    return client.createInvoice(
        Map.of(
            "patientId",
            charge.patientId(),
            "reference",
            "pharmacy:" + id,
            "amount",
            charge.amount(),
            "currency",
            c.currency()));
  }
}
