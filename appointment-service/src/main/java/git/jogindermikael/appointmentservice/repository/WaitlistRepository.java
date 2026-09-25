package git.jogindermikael.appointmentservice.repository;
import git.jogindermikael.appointmentservice.model.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;
public interface WaitlistRepository extends JpaRepository<WaitlistEntry, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    Optional<WaitlistEntry> findById(UUID id);
}
