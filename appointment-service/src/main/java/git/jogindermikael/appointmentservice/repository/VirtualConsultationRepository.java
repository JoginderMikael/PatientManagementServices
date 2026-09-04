package git.jogindermikael.appointmentservice.repository;
import git.jogindermikael.appointmentservice.model.AppointmentModels.VirtualConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface VirtualConsultationRepository extends JpaRepository<VirtualConsultation, UUID> {}
