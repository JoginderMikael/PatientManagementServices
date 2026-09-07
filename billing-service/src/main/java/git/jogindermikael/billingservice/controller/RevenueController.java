package git.jogindermikael.billingservice.controller;

import git.jogindermikael.billingservice.service.RevenueService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/billing/invoices")
@PreAuthorize("hasAnyRole('ADMIN','BILLING_STAFF')")
public class RevenueController {
  private final RevenueService service;

  public RevenueController(RevenueService service) {
    this.service = service;
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','BILLING_STAFF','PHARMACIST')")
  public Map<String, Object> create(@Valid @RequestBody RevenueService.InvoiceCommand c) {
    return service.invoice(c);
  }

  @GetMapping("/{id}")
  public Map<String, Object> get(@PathVariable UUID id) {
    return service.get(id);
  }

  @GetMapping("/patient/{id}")
  public List<Map<String, Object>> list(@PathVariable UUID id) {
    return service.forPatient(id);
  }

  @PostMapping("/{id}/postings")
  public Map<String, Object> post(
      @PathVariable UUID id, @Valid @RequestBody RevenueService.PaymentCommand c) {
    return service.post(id, c);
  }
}
