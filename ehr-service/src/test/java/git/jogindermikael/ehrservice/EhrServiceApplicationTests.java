package git.jogindermikael.ehrservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.audit.enabled=false")
class EhrServiceApplicationTests {
    @Test
    void contextLoads() {
    }
}
