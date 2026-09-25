package git.jogindermikael.ehrservice.repository;
import git.jogindermikael.ehrservice.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.UUID;
public interface PrescriptionRepository extends JpaRepository<Prescription,UUID>{List<Prescription> findByPatientId(UUID patientId);}
