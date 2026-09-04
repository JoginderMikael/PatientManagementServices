package git.jogindermikael.notificationservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"spring.kafka.listener.auto-startup=false", "app.audit.enabled=false"})
class NotificationServiceApplicationTests {
    @Test
    void contextLoads() {
    }
}
