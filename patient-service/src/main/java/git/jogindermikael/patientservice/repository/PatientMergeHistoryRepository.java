package git.jogindermikael.patientservice.repository;

import git.jogindermikael.patientservice.model.PatientMergeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PatientMergeHistoryRepository extends JpaRepository<PatientMergeHistory, UUID> {
    Optional<PatientMergeHistory> findFirstBySourcePatientIdAndUnmergedAtIsNullOrderByMergedAtDesc(UUID sourcePatientId);
}
