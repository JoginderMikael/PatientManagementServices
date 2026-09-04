package git.jogindermikael.billingservice;

import git.jogindermikael.billingservice.model.BillingAccount;
import git.jogindermikael.billingservice.repository.BillingAccountRepository;
import git.jogindermikael.billingservice.service.BillingAccountService;
import org.junit.jupiter.api.Test; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.test.context.SpringBootTest;
import java.util.UUID; import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={"spring.kafka.listener.auto-startup=false","app.audit.enabled=false","grpc.server.port=0"})
class Phase2BillingWorkflowTest {
    @Autowired BillingAccountService service; @Autowired BillingAccountRepository repository;
    @Test void accountCreationIsIdempotentByPatientAndCommand(){repository.deleteAll();UUID patientId=UUID.randomUUID();BillingAccount first=service.create(patientId,"command-1");BillingAccount replay=service.create(patientId,"command-1");BillingAccount anotherCommand=service.create(patientId,"command-2");assertEquals(first.getId(),replay.getId());assertEquals(first.getId(),anotherCommand.getId());assertEquals(1,repository.count());}
}
