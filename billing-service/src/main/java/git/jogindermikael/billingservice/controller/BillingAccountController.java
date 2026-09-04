package git.jogindermikael.billingservice.controller;

import git.jogindermikael.billingservice.dto.BillingAccountRequest;
import git.jogindermikael.billingservice.model.BillingAccount;
import git.jogindermikael.billingservice.service.BillingAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/billing/accounts")
@PreAuthorize("hasAnyRole('ADMIN','BILLING_STAFF')")
public class BillingAccountController {
    private final BillingAccountService service;
    public BillingAccountController(BillingAccountService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<BillingAccount> create(@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                                  @Valid @RequestBody BillingAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request.patientId(), idempotencyKey));
    }
    @GetMapping public List<BillingAccount> list() { return service.list(); }
    @GetMapping("/{id}") public BillingAccount get(@PathVariable UUID id) { return service.get(id); }
    @GetMapping("/patient/{patientId}") public BillingAccount forPatient(@PathVariable UUID patientId) { return service.forPatient(patientId); }
}
