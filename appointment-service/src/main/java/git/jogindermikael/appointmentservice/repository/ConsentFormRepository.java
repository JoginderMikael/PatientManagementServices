package git.jogindermikael.appointmentservice.repository;
import git.jogindermikael.appointmentservice.model.AppointmentModels.ConsentForm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface ConsentFormRepository extends JpaRepository<ConsentForm, UUID> {}
