package git.jogindermikael.patientservice;

import git.jogindermikael.patientservice.dto.PatientRequestDTO;
import git.jogindermikael.patientservice.dto.PatientResponseDTO;
import git.jogindermikael.patientservice.model.PatientStatus;
import git.jogindermikael.patientservice.repository.OutboxEventRepository;
import git.jogindermikael.patientservice.repository.PatientRepository;
import git.jogindermikael.patientservice.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={"app.audit.enabled=false","app.outbox.publish-delay-ms=3600000","grpc.server.port=0"})
@Transactional
class Phase2PatientWorkflowTest {
    @Autowired PatientService service; @Autowired PatientRepository patients; @Autowired OutboxEventRepository outbox;
    @Test void registrationIsIdempotentAndCreatesVersionedOutboxEvent(){
        PatientRequestDTO request=request("mpi-one@example.test","+15550000001");
        PatientResponseDTO first=service.createPatient(request,"registration-command-1");
        PatientResponseDTO replay=service.createPatient(request,"registration-command-1");
        assertEquals(first.getId(),replay.getId());assertTrue(first.getMrn().startsWith("MRN-"));assertEquals(1,patients.count());assertEquals(1,outbox.count());assertTrue(outbox.findAll().getFirst().getPayload().contains("\"schemaVersion\":1"));
    }
    @Test void mergeAndUnmergePreservePatientRecords(){
        PatientResponseDTO source=service.createPatient(request("source@example.test","+15550000002"),"source");
        PatientResponseDTO target=service.createPatient(request("target@example.test","+15550000003"),"target");
        assertEquals("MERGED",service.merge(java.util.UUID.fromString(source.getId()),java.util.UUID.fromString(target.getId())).getStatus());
        assertEquals("ACTIVE",service.unmerge(java.util.UUID.fromString(source.getId())).getStatus());assertEquals(2,patients.count());
    }
    private PatientRequestDTO request(String email,String phone){PatientRequestDTO request=new PatientRequestDTO();request.setName("Synthetic Patient");request.setEmail(email);request.setAddress("1 Test Street");request.setDateOfBirth("1990-01-01");request.setRegisteredDate("2026-09-04");request.setPhone(phone);request.setPreferredLanguage("en");request.setExternalIdentifiers(Map.of("TEST","EXT-"+phone));return request;}
}
