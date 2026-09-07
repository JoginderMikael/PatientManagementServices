package git.jogindermikael.billingservice.service;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class RevenueService {
  private final JdbcTemplate jdbc;
  private final BillingAccountService accounts;

  public RevenueService(JdbcTemplate jdbc, BillingAccountService accounts) {
    this.jdbc = jdbc;
    this.accounts = accounts;
  }

  public record InvoiceCommand(
      @NotNull UUID patientId,
      @NotBlank String reference,
      @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 17, fraction = 2)
          BigDecimal amount,
      @Pattern(regexp = "[A-Z]{3}") @NotNull String currency) {}

  public record PaymentCommand(
      @NotBlank String reference,
      @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 17, fraction = 2)
          BigDecimal amount,
      @Pattern(regexp = "PAYMENT|REMITTANCE") @NotNull String kind) {}

  public Map<String, Object> invoice(InvoiceCommand c) {
    jdbc.queryForObject(
        "SELECT id FROM revenue_creation_lock WHERE id=1 FOR UPDATE", Integer.class);
    var existing = jdbc.queryForList("SELECT * FROM invoice WHERE reference=?", c.reference());
    if (!existing.isEmpty()) {
      var row = existing.getFirst();
      if (!c.patientId().equals(row.get("patient_id"))
          || !c.currency().equals(row.get("currency"))
          || c.amount().compareTo((BigDecimal) row.get("amount")) != 0)
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Invoice reference reused");
      return row;
    }
    var account = accounts.create(c.patientId(), null);
    if (!account.getCurrency().equals(c.currency()))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Invoice currency must match the billing account");
    UUID id = UUID.randomUUID();
    jdbc.update(
        "INSERT INTO invoice VALUES (?,?,?,?,?,0,CURRENT_TIMESTAMP)",
        id,
        c.patientId(),
        c.reference(),
        c.amount(),
        c.currency());
    jdbc.update(
        "UPDATE billing_account SET"
            + " balance=balance+?,version=version+1,updated_at=CURRENT_TIMESTAMP WHERE"
            + " patient_id=?",
        c.amount(),
        c.patientId());
    return get(id);
  }

  public Map<String, Object> get(UUID id) {
    return jdbc.queryForList("SELECT * FROM invoice WHERE id=?", id).stream()
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
  }

  public List<Map<String, Object>> forPatient(UUID patient) {
    return jdbc.queryForList(
        "SELECT * FROM invoice WHERE patient_id=? ORDER BY created_at DESC", patient);
  }

  public Map<String, Object> post(UUID invoice, PaymentCommand c) {
    var rows = jdbc.queryForList("SELECT * FROM invoice WHERE id=? FOR UPDATE", invoice);
    if (rows.isEmpty())
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found");
    var old = jdbc.queryForList("SELECT * FROM payment_posting WHERE reference=?", c.reference());
    if (!old.isEmpty()) {
      var row = old.getFirst();
      if (!invoice.equals(row.get("invoice_id"))
          || c.amount().compareTo((BigDecimal) row.get("amount")) != 0
          || !c.kind().equals(row.get("kind")))
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment reference reused");
      return row;
    }
    var row = rows.getFirst();
    BigDecimal balance = ((BigDecimal) row.get("amount")).subtract((BigDecimal) row.get("paid"));
    if (c.amount().signum() <= 0 || c.amount().compareTo(balance) > 0)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment exceeds outstanding balance");
    UUID id = UUID.randomUUID();
    jdbc.update(
        "INSERT INTO payment_posting VALUES (?,?,?,?,?,CURRENT_TIMESTAMP)",
        id,
        invoice,
        c.reference(),
        c.amount(),
        c.kind());
    jdbc.update("UPDATE invoice SET paid=paid+? WHERE id=?", c.amount(), invoice);
    jdbc.update(
        "UPDATE billing_account SET"
            + " balance=balance-?,version=version+1,updated_at=CURRENT_TIMESTAMP WHERE"
            + " patient_id=?",
        c.amount(),
        row.get("patient_id"));
    return jdbc.queryForMap("SELECT * FROM payment_posting WHERE id=?", id);
  }
}
