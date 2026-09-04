package git.jogindermikael.appointmentservice.repository;
import git.jogindermikael.appointmentservice.model.AppointmentModels.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface WaitlistRepository extends JpaRepository<WaitlistEntry, UUID> {}
