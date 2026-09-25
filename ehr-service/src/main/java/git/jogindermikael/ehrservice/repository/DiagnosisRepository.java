package git.jogindermikael.ehrservice.repository;
import git.jogindermikael.ehrservice.model.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.UUID;
public interface DiagnosisRepository extends JpaRepository<Diagnosis,UUID>{List<Diagnosis> findByPatientId(UUID patientId);}
