package git.jogindermikael.ehrservice.repository;
import git.jogindermikael.ehrservice.model.EhrModels.LabResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.UUID;
public interface LabResultRepository extends JpaRepository<LabResult,UUID>{List<LabResult> findByPatientId(UUID patientId);}
