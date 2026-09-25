package git.jogindermikael.appointmentservice.repository;
import git.jogindermikael.appointmentservice.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface AppointmentJpaRepository extends JpaRepository<Appointment, UUID> {}
