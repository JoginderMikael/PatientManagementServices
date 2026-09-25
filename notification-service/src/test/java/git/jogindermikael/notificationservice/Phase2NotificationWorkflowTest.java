package git.jogindermikael.notificationservice;

import git.jogindermikael.notificationservice.dto.NotificationRequest;
import git.jogindermikael.notificationservice.model.NotificationMessage;
import git.jogindermikael.notificationservice.repository.NotificationRepository;
import git.jogindermikael.notificationservice.service.NotificationDispatcher;
import git.jogindermikael.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = { "spring.kafka.listener.auto-startup=false", "app.audit.enabled=false",
        "app.notifications.dispatch-delay-ms=3600000" })
class Phase2NotificationWorkflowTest {
    @Autowired
    NotificationService service;
    @Autowired
    NotificationDispatcher dispatcher;
    @Autowired
    NotificationRepository repository;

    @Test
    void queuedNotificationIsPersistedAndDeliveredByProvider() {
        repository.deleteAll();
        NotificationMessage queued = service.send(new NotificationRequest(UUID.randomUUID(), "EMAIL",
                "synthetic@example.test", "TEST", "No PHI", UUID.randomUUID()));
        assertEquals("QUEUED", queued.getStatus());
        dispatcher.dispatch();
        NotificationMessage sent = repository.findById(queued.getId()).orElseThrow();
        assertEquals("SENT", sent.getStatus());
        assertNotNull(sent.getProviderMessageId());
    }
}
