package git.jogindermikael.ehrservice;

import static org.junit.jupiter.api.Assertions.*;

import git.jogindermikael.ehrservice.dto.EhrDtos.ClinicalAmendmentRequest;
import git.jogindermikael.ehrservice.dto.EhrDtos.ClinicalResourceRequest;
import git.jogindermikael.ehrservice.dto.EhrDtos.TerminologyCodeRequest;
import git.jogindermikael.ehrservice.service.ClinicalResourceService;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
      "app.audit.enabled=false",
      "app.reliability.publish-initial-delay-ms=3600000"
    })
class Phase6StructuredClinicalResourceTest {
  @Autowired ClinicalResourceService service;

  @Test
  void versionsTerminologyValidatedObservationAndVerifiesHashChain() {
    UUID patientId = UUID.randomUUID();
    service.registerCode(
        new TerminologyCodeRequest(
            "http://loinc.org", "8867-4", "Heart rate", Set.of("OBSERVATION")));

    var created =
        service.create(
            new ClinicalResourceRequest(
                patientId,
                null,
                "OBSERVATION",
                "FINAL",
                "http://loinc.org",
                "8867-4",
                "Heart rate",
                Instant.now(),
                Map.of("value", 72, "unit", "beats/minute")));

    var amended =
        service.amend(
            created.resource().getId(),
            new ClinicalAmendmentRequest(
                "Corrected device transcription",
                "CORRECTED",
                Map.of("value", 70, "unit", "beats/minute")));

    assertEquals(2, amended.resource().getCurrentVersion());
    assertEquals(2, amended.provenance().size());
    assertTrue(service.verifyIntegrity(created.resource().getId()).valid());
    assertEquals(
        1,
        service.list(patientId, "OBSERVATION", 0, 20).getTotalElements());
  }
}
