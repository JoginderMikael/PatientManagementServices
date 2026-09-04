package git.jogindermikael.appointmentservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.audit.enabled=false")
class AppointmentServiceApplicationTests {
    @Test
    void contextLoads() {
    }
}
