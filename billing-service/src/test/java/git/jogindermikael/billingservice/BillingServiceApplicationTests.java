package git.jogindermikael.billingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"spring.kafka.listener.auto-startup=false", "app.audit.enabled=false", "grpc.server.port=0"})
class BillingServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
