package git.jogindermikael.appointmentservice;

import git.jogindermikael.appointmentservice.dto.AppointmentDtos.AppointmentRequest;
import git.jogindermikael.appointmentservice.service.AppointmentService;
import org.junit.jupiter.api.Test; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.test.context.SpringBootTest; import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime; import java.util.UUID; import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties="app.audit.enabled=false")
class Phase2AppointmentWorkflowTest {
    @Autowired AppointmentService service;
    @Test void overlappingProviderIntervalsAreRejected(){UUID doctor=UUID.randomUUID();LocalDateTime start=LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);service.bookAppointment(new AppointmentRequest(UUID.randomUUID(),doctor,start,"first",30));ResponseStatusException conflict=assertThrows(ResponseStatusException.class,()->service.bookAppointment(new AppointmentRequest(UUID.randomUUID(),doctor,start.plusMinutes(10),"overlap",30)));assertEquals(409,conflict.getStatusCode().value());}
}
