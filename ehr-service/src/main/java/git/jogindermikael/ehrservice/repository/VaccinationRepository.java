package git.jogindermikael.ehrservice.repository;
import git.jogindermikael.ehrservice.model.EhrModels.VaccinationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.UUID;
public interface VaccinationRepository extends JpaRepository<VaccinationRecord,UUID>{List<VaccinationRecord> findByPatientId(UUID patientId);}
