package git.jogindermikael.billingservice;

import static org.junit.jupiter.api.Assertions.*;

import git.jogindermikael.billingservice.dto.InvoiceCommand;
import git.jogindermikael.billingservice.dto.PaymentCommand;
import git.jogindermikael.billingservice.service.RevenueService;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(properties = {"app.audit.enabled=false", "grpc.server.port=-1"})
class Phase3RevenueTest {

  @Autowired RevenueService service;
  @Autowired git.jogindermikael.billingservice.service.BillingAccountService accounts;

  @Test
  void paymentsAreIdempotentAndCannotOverpay() {
    var invoice =
        service.invoice(
            new InvoiceCommand(
                UUID.randomUUID(), UUID.randomUUID().toString(), new BigDecimal("100.00"), "USD"));
    UUID id = (UUID) invoice.get("id");
    assertEquals(
        new BigDecimal("100.00"),
        accounts.forPatient((UUID) invoice.get("patient_id")).getBalance());
    var command =
        new PaymentCommand(
            UUID.randomUUID().toString(), new BigDecimal("70.00"), "REMITTANCE");
    var payment = service.post(id, command);
    assertEquals(payment.get("id"), service.post(id, command).get("id"));
    assertThrows(
        ResponseStatusException.class,
        () ->
            service.post(
                id,
                new PaymentCommand(
                    UUID.randomUUID().toString(), new BigDecimal("31.00"), "PAYMENT")));
    assertEquals(new BigDecimal("70.00"), service.get(id).get("paid"));
    assertEquals(
        new BigDecimal("30.00"),
        accounts.forPatient((UUID) invoice.get("patient_id")).getBalance());
  }

  @Test
  void concurrentPaymentsCannotOverdrawInvoice() throws Exception {
    UUID id =
        (UUID)
            service
                .invoice(
                    new InvoiceCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID().toString(),
                        new BigDecimal("10.00"),
                        "USD"))
                .get("id");
    try (var pool = java.util.concurrent.Executors.newFixedThreadPool(2)) {
      var barrier = new java.util.concurrent.CyclicBarrier(2);
      var jobs = new ArrayList<java.util.concurrent.Callable<Boolean>>();
      for (int i = 0; i < 2; i++)
        jobs.add(
            () -> {
              barrier.await();
              try {
                service.post(
                    id,
                    new PaymentCommand(
                        UUID.randomUUID().toString(), new BigDecimal("10.00"), "PAYMENT"));
                return true;
              } catch (ResponseStatusException e) {
                return false;
              }
            });
      int successes = 0;
      for (var f : pool.invokeAll(jobs)) if (f.get()) successes++;
      assertEquals(1, successes);
    }
    assertEquals(new BigDecimal("10.00"), service.get(id).get("paid"));
  }
}
