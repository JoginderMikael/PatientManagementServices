package git.jogindermikael.ehrservice.repository;
import git.jogindermikael.ehrservice.model.Encounter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.UUID;
public interface EncounterRepository extends JpaRepository<Encounter,UUID>{List<Encounter> findByPatientIdOrderByStartedAtDesc(UUID patientId);}
