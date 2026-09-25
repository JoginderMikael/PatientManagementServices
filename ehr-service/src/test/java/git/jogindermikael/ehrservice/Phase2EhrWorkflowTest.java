package git.jogindermikael.ehrservice;

import git.jogindermikael.ehrservice.dto.*; import git.jogindermikael.ehrservice.model.*; import git.jogindermikael.ehrservice.service.EhrService;
import org.junit.jupiter.api.Test; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDate; import java.util.UUID; import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties="app.audit.enabled=false")
class Phase2EhrWorkflowTest {
    @Autowired EhrService service;
    @Test void persistsEncounterLinkedClinicalRecord(){UUID patient=UUID.randomUUID(),clinician=UUID.randomUUID();Encounter encounter=service.startEncounter(new EncounterRequest(patient,clinician,null,"annual review"));Diagnosis diagnosis=service.recordDiagnosis(new DiagnosisRequest(patient,clinician,"Z00.00","General examination",LocalDate.now(),encounter.getId()));ClinicalNote note=service.addNote(new ClinicalNoteRequest(encounter.getId(),patient,clinician,"Synthetic clinical note"));assertEquals(encounter.getId(),diagnosis.getEncounterId());assertEquals("SIGNED",service.signNote(note.getId()).getStatus());assertEquals(1,service.encountersForPatient(patient).size());assertEquals(1,service.notesForPatient(patient).size());}
}
