package git.jogindermikael.ehrservice.repository;
import git.jogindermikael.ehrservice.model.EhrModels.ClinicalNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.UUID;
public interface ClinicalNoteRepository extends JpaRepository<ClinicalNote,UUID>{List<ClinicalNote> findByPatientIdOrderByCreatedAtDesc(UUID patientId);}
