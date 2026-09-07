package git.jogindermikael.ehrservice;

import static org.junit.jupiter.api.Assertions.*;

import git.jogindermikael.ehrservice.dto.EhrDtos.*;
import git.jogindermikael.ehrservice.service.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(properties = {"app.audit.enabled=false", "grpc.server.port=-1"})
class Phase3ClinicalSafetyTest {

  @Autowired EhrService service;
  @Autowired ClinicalSafetyService safety;

  @Test
  void dispensingRechecksNewAllergiesAndDiscontinuedOrders() {
    UUID patient = UUID.randomUUID(), clinician = UUID.randomUUID();
    var prescription =
        service.prescribe(
            new PrescriptionRequest(patient, clinician, "Synthetic-Safety", "test", "test", null));
    assertEquals(patient, safety.dispensingSafety(prescription.id()).get("patientId"));
    service.recordHistory(
        new MedicalHistoryRequest(
            patient, "Updated allergy history", List.of("Synthetic-Safety"), List.of()));
    assertThrows(ResponseStatusException.class, () -> safety.dispensingSafety(prescription.id()));
    safety.discontinue(prescription.id());
    assertThrows(ResponseStatusException.class, () -> safety.dispensingSafety(prescription.id()));
  }

  @Test
  void allergyAndDuplicateMedicationBlockPrescribing() {
    UUID patient = UUID.randomUUID(), clinician = UUID.randomUUID();
    service.recordHistory(
        new MedicalHistoryRequest(patient, "Synthetic", List.of("Synthetic-X"), List.of()));
    assertThrows(
        ResponseStatusException.class,
        () ->
            service.prescribe(
                new PrescriptionRequest(
                    patient, clinician, "Synthetic-X", "test dose", "test instructions", null)));
    service.prescribe(
        new PrescriptionRequest(
            patient, clinician, "Synthetic-Y", "test dose", "test instructions", null));
    assertThrows(
        ResponseStatusException.class,
        () ->
            service.prescribe(
                new PrescriptionRequest(
                    patient, clinician, "Synthetic-Y", "test dose", "test instructions", null)));
  }

  @Test
  void configuredInteractionBlocksAndClosedEncounterRejectsChanges() {
    UUID patient = UUID.randomUUID(), clinician = UUID.randomUUID();
    String first = UUID.randomUUID().toString(), second = UUID.randomUUID().toString();
    safety.rule(new ClinicalSafetyService.Rule(first, second, "Synthetic interaction for testing"));
    service.prescribe(new PrescriptionRequest(patient, clinician, first, "test", "test", null));
    assertThrows(
        ResponseStatusException.class,
        () ->
            service.prescribe(
                new PrescriptionRequest(patient, clinician, second, "test", "test", null)));
    var encounter = service.startEncounter(new EncounterRequest(patient, clinician, null, "test"));
    service.closeEncounter(encounter.id());
    assertThrows(
        ResponseStatusException.class,
        () ->
            service.addLabResult(
                new LabResultRequest(
                    patient, "test", "test", "test", LocalDate.now(), encounter.id())));
  }
}
