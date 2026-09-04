package git.jogindermikael.appointmentservice.repository;
import git.jogindermikael.appointmentservice.model.AppointmentModels.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, UUID> {
    void deleteByAppointmentId(UUID appointmentId);
}
