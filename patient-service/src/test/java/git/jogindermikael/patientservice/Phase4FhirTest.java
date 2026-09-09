package git.jogindermikael.patientservice;

import git.jogindermikael.patientservice.model.Patient;
import git.jogindermikael.patientservice.repository.PatientRepository;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"app.audit.enabled=false","app.outbox.publish-delay-ms=3600000","grpc.server.port=0"})
@AutoConfigureMockMvc
@Transactional
class Phase4FhirTest {
    @Autowired MockMvc mvc;
    @Autowired PatientRepository patients;
    static final AtomicReference<String> decision = new AtomicReference<>("{\"allowed\":true}");
    static final AtomicReference<String> requestedPath = new AtomicReference<>();
    static final AtomicReference<String> bearer = new AtomicReference<>();
    static final HttpServer privacyServer;
    static {
        try {
            privacyServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            privacyServer.createContext("/compliance/privacy/decisions", exchange -> {
                requestedPath.set(exchange.getRequestURI().toString());
                bearer.set(exchange.getRequestHeaders().getFirst("Authorization"));
                byte[] body = decision.get().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            privacyServer.start();
        } catch (java.io.IOException exception) { throw new ExceptionInInitializerError(exception); }
    }
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.privacy.base-url", () -> "http://127.0.0.1:" + privacyServer.getAddress().getPort());
    }
    @BeforeEach void reset() { decision.set("{\"allowed\":true}"); requestedPath.set(null); bearer.set(null); }

    @Test void capabilityOnlyAdvertisesImplementedReadInteraction() throws Exception {
        mvc.perform(get("/fhir/metadata").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLINICIAN"))))
                .andExpect(status().isOk()).andExpect(content().contentType("application/fhir+json"))
                .andExpect(jsonPath("$.resourceType").value("CapabilityStatement"))
                .andExpect(jsonPath("$.fhirVersion").value("4.0.1"))
                .andExpect(jsonPath("$.rest[0].resource[0].interaction.length()").value(1))
                .andExpect(jsonPath("$.rest[0].resource[0].interaction[0].code").value("read"))
                .andDo(result -> saveFixture("capability", result.getResponse().getContentAsString()));
    }
    @Test void patientResourceHasFhirShapeAndNoInternalRegistrationFields() throws Exception {
        Patient patient = new Patient();
        patient.setName("Synthetic Patient"); patient.setEmail("fhir@example.test");
        patient.setAddress("Test address"); patient.setMrn("FHIR-001");
        patient.setDateOfBirth(LocalDate.of(1990,1,1)); patient.setRegisteredDate(LocalDate.now());
        patient = patients.saveAndFlush(patient);
        mvc.perform(get("/fhir/Patient/" + patient.getId()).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLINICIAN"))))
                .andExpect(status().isOk()).andExpect(content().contentType("application/fhir+json"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().exists("ETag")).andExpect(header().exists("Last-Modified"))
                .andExpect(jsonPath("$.resourceType").value("Patient"))
                .andExpect(jsonPath("$.identifier[0].value").value("FHIR-001"))
                .andExpect(jsonPath("$.name[0].text").value("Synthetic Patient"))
                .andExpect(jsonPath("$.birthDate").value("1990-01-01"))
                .andExpect(jsonPath("$.registrationKey").doesNotExist())
                .andDo(result -> saveFixture("patient", result.getResponse().getContentAsString()));
        assertEquals("/compliance/privacy/decisions/" + patient.getId(), requestedPath.get());
        assertEquals("Bearer token", bearer.get());
    }
    @Test void deniedOrUnavailablePrivacyNeverReturnsPatientData() throws Exception {
        UUID id = UUID.randomUUID();
        decision.set("{\"allowed\":false}");
        mvc.perform(get("/fhir/Patient/" + id).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.resourceType").value("OperationOutcome"))
                .andDo(result -> saveFixture("outcome", result.getResponse().getContentAsString()));
        decision.set("invalid json");
        mvc.perform(get("/fhir/Patient/" + id).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.issue[0].code").value("transient"));
    }
    @Test void rejectsAnonymousAndPatientRolesAndReturnsOutcomeForInvalidId() throws Exception {
        mvc.perform(get("/fhir/metadata")).andExpect(status().isUnauthorized());
        mvc.perform(get("/fhir/metadata").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))).andExpect(status().isForbidden());
        mvc.perform(get("/fhir/Patient/invalid").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLINICIAN"))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.issue[0].code").value("invalid"));
        assertNull(requestedPath.get());
    }
    @Test void malformedDecisionShapesFailClosedAsUnavailable() throws Exception {
        for (String body : new String[]{"{}", "null", "{\"allowed\":null}", "{\"allowed\":\"true\"}", "{\"allowed\":1}"}) {
            decision.set(body);
            mvc.perform(get("/fhir/Patient/" + UUID.randomUUID())
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLINICIAN"))))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.issue[0].code").value("transient"))
                    .andExpect(jsonPath("$.name").doesNotExist());
        }
    }
    @Test void forwardsExplicitEmergencyGrantWithoutChangingPatientOrBearer() throws Exception {
        UUID patient = UUID.randomUUID();
        UUID emergency = UUID.randomUUID();
        decision.set("{\"allowed\":false}");
        mvc.perform(get("/fhir/Patient/" + patient).header("X-Break-Glass-Id", emergency)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLINICIAN"))))
                .andExpect(status().isForbidden());
        assertEquals("/compliance/privacy/decisions/" + patient + "?emergencyId=" + emergency, requestedPath.get());
        assertEquals("Bearer token", bearer.get());
    }
    private static void saveFixture(String name, String json) throws java.io.IOException {
        java.nio.file.Path directory = java.nio.file.Path.of("target", "fhir-conformance");
        java.nio.file.Files.createDirectories(directory);
        java.nio.file.Files.writeString(directory.resolve(name + ".json"), json);
    }
}
