package git.jogindermikael.appointmentservice.repository;

import git.jogindermikael.appointmentservice.model.AppointmentModels.AppointmentResourceSlot;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentResourceSlotRepository
    extends JpaRepository<AppointmentResourceSlot, UUID> {
  void deleteByAppointmentId(UUID appointmentId);
}
