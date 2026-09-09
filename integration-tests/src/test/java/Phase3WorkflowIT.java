import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;

/** Runs packaged services with independent PostgreSQL databases and real signed HTTP requests. */
class Phase3WorkflowIT {
  private static final String KEY = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final HttpClient HTTP =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
  private static final PostgreSQLContainer<?> DB = new PostgreSQLContainer<>("postgres:16-alpine");
  private static final Map<String, Integer> PORTS = new LinkedHashMap<>();
  private static final List<Process> PROCESSES = new CopyOnWriteArrayList<>();
  private static Path root;
  private static String admin;

  @BeforeAll
  static void start() throws Exception {
    root = Path.of(System.getProperty("maven.multiModuleProjectDirectory", "..")).toAbsolutePath();
    if (!Files.isDirectory(root.resolve("billing-service"))) root = root.getParent();
    DB.start();
    admin = token(UUID.randomUUID().toString(), "ADMIN");
    for (String module :
        List.of(
            "patient-service",
            "audit-compliance-service",
            "billing-service",
            "ehr-service",
            "insurance-service",
            "inventory-pharmacy-service",
            "patient-portal-service",
            "staff-dashboard-service")) {
      try (ServerSocket socket = new ServerSocket(0)) {
        PORTS.put(module, socket.getLocalPort());
      }
      try (Connection connection = DB.createConnection("");
          Statement sql = connection.createStatement()) {
        sql.execute("CREATE DATABASE " + module.replace('-', '_'));
      }
    }
    try (var executor = Executors.newFixedThreadPool(6)) {
      var starts = new ArrayList<Future<?>>();
      PORTS.forEach(
          (module, port) ->
              starts.add(
                  executor.submit(
                      () -> {
                        try {
                          startService(module, port);
                        } catch (Exception exception) {
                          throw new RuntimeException(exception);
                        }
                      })));
      for (var start : starts) start.get(150, TimeUnit.SECONDS);
    } catch (Exception error) {
      stop();
      throw error;
    }
  }

  private static void startService(String module, int port) throws Exception {
    Path jar = root.resolve(module + "/target/" + module + "-0.0.1-SNAPSHOT.jar");
    assertTrue(Files.isRegularFile(jar), "Run mvn verify to package " + module);
    Path log = root.resolve("integration-tests/target/" + module + "-e2e.log");
    var builder =
        new ProcessBuilder(
            Path.of(System.getProperty("java.home"), "bin", "java").toString(),
            "-Xmx256m",
            "-jar",
            jar.toString(),
            "--server.port=" + port,
            "--spring.datasource.url=jdbc:postgresql://"
                + DB.getHost()
                + ":"
                + DB.getMappedPort(5432)
                + "/"
                + module.replace('-', '_'),
            "--spring.datasource.username=" + DB.getUsername(),
            "--spring.datasource.password=" + DB.getPassword(),
            "--jwt.secret=" + KEY,
            "--jwt.issuer=patient-management-auth",
            "--jwt.audience=patient-management-api",
            "--app.audit.enabled=false",
            "--app.outbox.publish-delay-ms=3600000",
            "--app.privacy.base-url=http://localhost:" + PORTS.get("audit-compliance-service"),
            "--grpc.server.port=-1",
            "--spring.kafka.listener.auto-startup=false",
            "--app.billing.url=http://localhost:" + PORTS.get("billing-service"),
            "--app.staff.url=http://localhost:" + PORTS.get("staff-dashboard-service"),
            "--app.ehr.url=http://localhost:" + PORTS.get("ehr-service"),
            "--app.tasks.escalation-delay-ms=3600000",
            "--logging.level.root=WARN");
    Process process = builder.redirectErrorStream(true).redirectOutput(log.toFile()).start();
    PROCESSES.add(process);
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(120);
    while (System.nanoTime() < deadline && process.isAlive()) {
      try {
        var result =
            HTTP.send(
                HttpRequest.newBuilder(
                        URI.create("http://localhost:" + port + "/actuator/health/liveness"))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.discarding());
        if (result.statusCode() == 200) return;
      } catch (Exception ignored) {
      }
      Thread.sleep(300);
    }
    fail("Service failed to start: " + module + "; inspect " + log);
  }

  @AfterAll
  static void stop() {
    for (Process process : PROCESSES) {
      process.destroy();
      try {
        if (!process.waitFor(5, TimeUnit.SECONDS)) process.destroyForcibly();
      } catch (InterruptedException error) {
        Thread.currentThread().interrupt();
        process.destroyForcibly();
      }
    }
    DB.stop();
  }

  @Test
  void phase4ConsentAndEmergencyAccessAcrossSignedHttpServices() throws Exception {
    String subject = UUID.randomUUID().toString();
    String clinician = token(subject, "CLINICIAN");
    String officer = token(UUID.randomUUID().toString(), "PRIVACY_OFFICER");
    String other = token(UUID.randomUUID().toString(), "CLINICIAN");
    var patient = post("patient-service", "/patients", Map.of(
        "name", "Synthetic FHIR Patient", "email", "phase4@example.test", "address", "Test address",
        "dateOfBirth", "1990-01-01", "registeredDate", "2026-09-08"), admin, 200);
    String id = patient.get("id").toString();
    String path = "/fhir/Patient/" + id;
    assertEquals(403, call("patient-service", "GET", path, null, clinician, Map.of()).statusCode());
    var consent = post("audit-compliance-service", "/compliance/privacy/consents", Map.of(
        "patientId", id, "subject", subject, "expiresAt", Instant.now().plusSeconds(1800).toString(),
        "evidenceReference", "synthetic-consent"), officer, 200);
    var permitted = call("patient-service", "GET", path, null, clinician, Map.of());
    assertEquals(200, permitted.statusCode(), permitted.body());
    assertEquals("Patient", JSON.readTree(permitted.body()).path("resourceType").asText());
    assertEquals(403, call("patient-service", "GET", path, null, other, Map.of()).statusCode());
    post("audit-compliance-service", "/compliance/privacy/grants/" + consent.get("id") + "/revoke", null, officer, 200);
    assertEquals(403, call("patient-service", "GET", path, null, clinician, Map.of()).statusCode());
    var emergency = post("audit-compliance-service", "/compliance/privacy/break-glass", Map.of(
        "patientId", id, "expiresAt", Instant.now().plusSeconds(600).toString(), "evidenceReference", "synthetic-emergency"), clinician, 200);
    Map<String,String> emergencyHeader = Map.of("X-Break-Glass-Id", emergency.get("id").toString());
    assertEquals(403, call("patient-service", "GET", path, null, clinician, Map.of()).statusCode());
    assertEquals(200, call("patient-service", "GET", path, null, clinician, emergencyHeader).statusCode());
    assertEquals(403, call("patient-service", "GET", path, null, other, emergencyHeader).statusCode());
    post("audit-compliance-service", "/compliance/privacy/break-glass/" + emergency.get("id") + "/review",
        Map.of("evidenceReference", "synthetic-review"), officer, 200);
    assertEquals(403, call("patient-service", "GET", path, null, clinician, emergencyHeader).statusCode());
    var events = call("audit-compliance-service", "GET", "/audit/events?patientId=" + id, null, admin, Map.of());
    assertEquals(200, events.statusCode());
    assertTrue(events.body().contains("BREAK_GLASS_USE"));
    assertTrue(events.body().contains("GRANT_REVOKED"));
  }

  @Test
  void revenuePharmacyPortalAndClinicalWorkflows() throws Exception {
    String patient = UUID.randomUUID().toString(), clinician = UUID.randomUUID().toString();
    String invoice =
        post(
                "billing-service",
                "/billing/invoices",
                Map.of(
                    "patientId",
                    patient,
                    "reference",
                    UUID.randomUUID().toString(),
                    "amount",
                    100,
                    "currency",
                    "USD"),
                admin,
                200)
            .get("id")
            .toString();
    String policy =
        post(
                "insurance-service",
                "/insurance/policies",
                Map.of(
                    "patientId",
                    patient,
                    "providerName",
                    "Synthetic Payer",
                    "memberNumber",
                    UUID.randomUUID().toString(),
                    "planName",
                    "Test"),
                admin,
                201)
            .get("id")
            .toString();
    post(
        "insurance-service",
        "/insurance/policies/" + policy + "/eligibility-evidence",
        Map.of(
            "serviceCode",
            "TEST",
            "validUntil",
            LocalDate.now().plusDays(1).toString(),
            "payerReference",
            "test-evidence",
            "insurancePercent",
            70),
        admin,
        200);
    var eligibility =
        post(
            "insurance-service",
            "/insurance/coverage-verifications",
            Map.of("policyId", policy, "serviceCode", "TEST", "estimatedCharge", 100),
            admin,
            201);
    assertEquals("VERIFIED", eligibility.get("status"));
    String claim =
        post(
                "insurance-service",
                "/insurance/claims",
                Map.of(
                    "patientId", patient, "policyId", policy, "invoiceId", invoice, "amount", 100),
                admin,
                201)
            .get("id")
            .toString();
    post(
        "insurance-service",
        "/insurance/claims/" + claim + "/adjudicate",
        Map.of("status", "APPROVED", "approvedAmount", 70),
        admin,
        200);
    post(
        "insurance-service",
        "/insurance/claims/" + claim + "/remittances",
        Map.of("reference", "payer-" + claim, "paidAmount", 70),
        admin,
        200);
    var reconciliation =
        post(
            "insurance-service", "/insurance/claims/" + claim + "/reconcile", Map.of(), admin, 200);
    assertEquals(
        reconciliation.get("id"),
        post("insurance-service", "/insurance/claims/" + claim + "/reconcile", Map.of(), admin, 200)
            .get("id"));

    String owner = UUID.randomUUID().toString(), proxy = UUID.randomUUID().toString();
    String ownerToken = token(owner, "PATIENT"), proxyToken = token(proxy, "PATIENT");
    post(
        "patient-portal-service",
        "/portal/identities",
        Map.of("subject", owner, "patientId", patient),
        admin,
        200);
    var grantResponse =
        call(
            "patient-portal-service",
            "POST",
            "/portal/patients/" + patient + "/proxies",
            Map.of(
                "proxySubject",
                proxy,
                "scope",
                "RECORDS",
                "expiresAt",
                Instant.now().plusSeconds(300).toString()),
            ownerToken,
            Map.of());
    assertEquals(200, grantResponse.statusCode());
    String grant = JSON.readValue(grantResponse.body(), String.class);
    String record =
        post(
                "patient-portal-service",
                "/portal/record-requests",
                Map.of("patientId", patient, "recordType", "SUMMARY"),
                proxyToken,
                201)
            .get("id")
            .toString();
    post(
        "patient-portal-service",
        "/portal/record-requests/" + record + "/release",
        Map.of("content", "Synthetic released record"),
        admin,
        200);
    assertEquals(
        "Synthetic released record",
        call(
                "patient-portal-service",
                "GET",
                "/portal/record-requests/" + record + "/download",
                null,
                proxyToken,
                Map.of())
            .body());
    assertEquals(
        200,
        call(
                "patient-portal-service",
                "DELETE",
                "/portal/patients/" + patient + "/proxies/" + grant,
                null,
                ownerToken,
                Map.of())
            .statusCode());
    assertEquals(
        403,
        call(
                "patient-portal-service",
                "GET",
                "/portal/record-requests/" + record + "/download",
                null,
                proxyToken,
                Map.of())
            .statusCode());
    assertEquals(
        403,
        call(
                "patient-portal-service",
                "GET",
                "/portal/patients/" + UUID.randomUUID() + "/overview",
                null,
                ownerToken,
                Map.of())
            .statusCode());
    var paymentResponse =
        call(
            "patient-portal-service",
            "POST",
            "/portal/payments",
            Map.of("patientId", patient, "invoiceId", invoice, "amount", 30),
            ownerToken,
            Map.of("Idempotency-Key", "portal-" + invoice));
    assertEquals(201, paymentResponse.statusCode());
    String payment = JSON.readTree(paymentResponse.body()).get("id").asText();
    post(
        "patient-portal-service",
        "/portal/payments/" + payment + "/settle",
        Map.of("providerReference", "receipt-" + payment),
        admin,
        200);
    var paid =
        JSON.readTree(
                call(
                        "billing-service",
                        "GET",
                        "/billing/invoices/" + invoice,
                        null,
                        admin,
                        Map.of())
                    .body())
            .get("paid")
            .decimalValue();
    assertEquals(0, paid.compareTo(new java.math.BigDecimal("100.00")));

    String medication =
        post(
                "inventory-pharmacy-service",
                "/inventory-pharmacy/medications",
                Map.of(
                    "name",
                    "Synthetic-M",
                    "ndcCode",
                    "test-code",
                    "quantityOnHand",
                    0,
                    "unitCost",
                    2),
                admin,
                201)
            .get("id")
            .toString();
    post(
        "inventory-pharmacy-service",
        "/inventory-pharmacy/batches",
        Map.of(
            "medicationId",
            medication,
            "lot",
            "test-lot",
            "expiresOn",
            LocalDate.now().plusDays(30).toString(),
            "quantity",
            10,
            "reference",
            "receipt-" + medication),
        admin,
        200);
    String rx =
        post(
                "ehr-service",
                "/ehr/prescriptions",
                Map.of(
                    "patientId",
                    patient,
                    "clinicianId",
                    clinician,
                    "medication",
                    "Synthetic-M",
                    "dosage",
                    "test dose",
                    "instructions",
                    "test instructions"),
                admin,
                201)
            .get("id")
            .toString();
    String pharmacy =
        post(
                "inventory-pharmacy-service",
                "/inventory-pharmacy/prescriptions",
                Map.of(
                    "ehrPrescriptionId",
                    rx,
                    "patientId",
                    patient,
                    "medication",
                    "Synthetic-M",
                    "quantity",
                    2),
                admin,
                201)
            .get("id")
            .toString();
    post(
        "inventory-pharmacy-service",
        "/inventory-pharmacy/prescriptions/" + pharmacy + "/dispense",
        Map.of(),
        admin,
        409);
    post(
        "inventory-pharmacy-service",
        "/inventory-pharmacy/prescriptions/" + pharmacy + "/review",
        Map.of(
            "medicationId",
            medication,
            "allergiesChecked",
            true,
            "interactionsChecked",
            true,
            "doseChecked",
            true,
            "reason",
            "Synthetic review"),
        admin,
        200);
    post(
        "inventory-pharmacy-service",
        "/inventory-pharmacy/prescriptions/" + pharmacy + "/dispense",
        Map.of(),
        admin,
        200);
    var pharmacyInvoice =
        post(
            "inventory-pharmacy-service",
            "/inventory-pharmacy/prescriptions/" + pharmacy + "/bill",
            Map.of("currency", "USD"),
            admin,
            200);
    assertEquals(
        pharmacyInvoice.get("id"),
        post(
                "inventory-pharmacy-service",
                "/inventory-pharmacy/prescriptions/" + pharmacy + "/bill",
                Map.of("currency", "USD"),
                admin,
                200)
            .get("id"));
    String alert =
        post(
                "ehr-service",
                "/ehr/safety/alerts",
                Map.of(
                    "patientId",
                    patient,
                    "assigneeId",
                    clinician,
                    "sourceReference",
                    "test-result-" + patient,
                    "summary",
                    "Synthetic critical result"),
                admin,
                200)
            .get("id")
            .toString();
    String task =
        post("ehr-service", "/ehr/safety/alerts/" + alert + "/escalate", Map.of(), admin, 200)
            .get("id")
            .toString();
    assertEquals(
        task,
        post("ehr-service", "/ehr/safety/alerts/" + alert + "/escalate", Map.of(), admin, 200)
            .get("id")
            .toString());
    String clinicianToken = token(clinician, "CLINICIAN");
    post(
        "staff-dashboard-service",
        "/staff-dashboard/tasks/" + task + "/claim",
        Map.of(),
        clinicianToken,
        200);
    post(
        "staff-dashboard-service",
        "/staff-dashboard/tasks/" + task + "/complete",
        Map.of(),
        clinicianToken,
        200);
    assertEquals(
        403,
        call(
                "staff-dashboard-service",
                "GET",
                "/staff-dashboard/queues/CLINICIAN",
                null,
                token(UUID.randomUUID().toString(), "BILLING_STAFF"),
                Map.of())
            .statusCode());
    loadCheck(invoice);
  }

  private void loadCheck(String invoice) throws Exception {
    assertEquals(
        401,
        call("billing-service", "GET", "/billing/invoices/" + invoice, null, "invalid", Map.of())
            .statusCode());
    for (int i = 0; i < 40; i++)
      post(
          "staff-dashboard-service",
          "/staff-dashboard/clinical-tasks",
          Map.of(
              "reference",
              UUID.randomUUID().toString(),
              "patientId",
              UUID.randomUUID().toString(),
              "assigneeId",
              UUID.randomUUID().toString(),
              "title",
              "Synthetic load task"),
          admin,
          200);

    var latencies = new CopyOnWriteArrayList<Long>();
    int requests = Integer.getInteger("phase3.load.requests", 100);
    long budget = Long.getLong("phase3.load.p95Millis", 2000);
    try (var pool = Executors.newFixedThreadPool(8)) {
      var jobs = new ArrayList<Callable<Void>>();
      for (int i = 0; i < requests; i++) {
        final boolean billing = i % 2 == 0;
        jobs.add(
            () -> {
              long start = System.nanoTime();
              var result =
                  call(
                      billing ? "billing-service" : "staff-dashboard-service",
                      "GET",
                      billing
                          ? "/billing/invoices/" + invoice
                          : "/staff-dashboard/queues/CLINICIAN",
                      null,
                      admin,
                      Map.of());
              assertEquals(200, result.statusCode());
              latencies.add(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
              return null;
            });
      }
      for (var result : pool.invokeAll(jobs)) result.get();
    }
    Collections.sort(latencies);
    long p95 = latencies.get((int) Math.ceil(latencies.size() * .95) - 1);
    Files.writeString(
        root.resolve("integration-tests/target/phase3-performance.json"),
        JSON.writeValueAsString(
            Map.of(
                "requests", requests, "concurrency", 8, "p95Millis", p95, "budgetMillis", budget)));
    assertTrue(p95 < budget, "Read workload p95 exceeded " + budget + "ms: " + p95);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> post(
      String service, String path, Object body, String bearer, int expected) throws Exception {
    var response = call(service, "POST", path, body, bearer, Map.of());
    assertEquals(expected, response.statusCode(), service + " " + path + " " + response.body());
    return response.body().isBlank() ? Map.of() : JSON.readValue(response.body(), Map.class);
  }

  private static HttpResponse<String> call(
      String service,
      String method,
      String path,
      Object body,
      String bearer,
      Map<String, String> headers)
      throws Exception {
    var builder =
        HttpRequest.newBuilder(URI.create("http://localhost:" + PORTS.get(service) + path))
            .timeout(Duration.ofSeconds(15))
            .header("Authorization", "Bearer " + bearer)
            .header("Content-Type", "application/json");
    headers.forEach(builder::header);
    return HTTP.send(
        builder
            .method(
                method,
                body == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body)))
            .build(),
        HttpResponse.BodyHandlers.ofString());
  }

  private static String token(String subject, String role) throws Exception {
    var base64 = Base64.getUrlEncoder().withoutPadding();
    String header =
        base64.encodeToString(
            "{\"alg\":\"HS256\",\"typ\":\"JWT\"}"
                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
    String payload =
        base64.encodeToString(
            JSON.writeValueAsBytes(
                Map.of(
                    "sub",
                    subject,
                    "role",
                    role,
                    "iss",
                    "patient-management-auth",
                    "aud",
                    List.of("patient-management-api"),
                    "iat",
                    Instant.now().getEpochSecond(),
                    "exp",
                    Instant.now().plusSeconds(900).getEpochSecond())));
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(Base64.getDecoder().decode(KEY), "HmacSHA256"));
    String content = header + "." + payload;
    return content
        + "."
        + base64.encodeToString(
            mac.doFinal(content.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
  }
}
