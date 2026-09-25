package git.jogindermikael.appointmentservice.repository;

import git.jogindermikael.appointmentservice.model.AppointmentResource;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentResourceRepository extends JpaRepository<AppointmentResource, UUID> {}
