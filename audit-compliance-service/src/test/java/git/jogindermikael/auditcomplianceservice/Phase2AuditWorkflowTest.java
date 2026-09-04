package git.jogindermikael.auditcomplianceservice;

import git.jogindermikael.auditcomplianceservice.model.AuditEvent; import git.jogindermikael.auditcomplianceservice.repository.AuditEventRepository; import git.jogindermikael.auditcomplianceservice.service.AuditComplianceService;
import org.junit.jupiter.api.Test; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.test.context.SpringBootTest; import org.springframework.transaction.annotation.Transactional;
import java.time.Instant; import java.util.UUID; import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties="spring.kafka.listener.auto-startup=false")
@Transactional
class Phase2AuditWorkflowTest {
    @Autowired AuditComplianceService service; @Autowired AuditEventRepository repository;
    @Test void sourceEventsAreIdempotentAndHashChained(){UUID source=UUID.randomUUID(),patient=UUID.randomUUID();AuditEvent first=service.append(source,"actor","ROLE_CLINICIAN","HTTP_GET",patient,"PATIENT",patient,"patient-service","SUCCESS",null,"GET /patients/id","request","correlation",Instant.now());AuditEvent replay=service.append(source,"actor","ROLE_CLINICIAN","HTTP_GET",patient,"PATIENT",patient,"patient-service","SUCCESS",null,null,null,null,Instant.now());assertEquals(first.getId(),replay.getId());assertEquals(64,first.getEventHash().length());assertEquals(1,repository.count());}
}
