package git.jogindermikael.appointmentservice.repository;
import git.jogindermikael.appointmentservice.model.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
public interface DoctorScheduleJpaRepository extends JpaRepository<DoctorSchedule, UUID> {
    Optional<DoctorSchedule> findByDoctorIdAndWorkDate(UUID doctorId, LocalDate workDate);
}
