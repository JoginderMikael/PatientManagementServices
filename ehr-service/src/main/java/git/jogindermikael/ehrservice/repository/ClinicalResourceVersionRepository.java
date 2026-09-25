package git.jogindermikael.ehrservice.repository;

import git.jogindermikael.ehrservice.model.ClinicalResourceVersion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicalResourceVersionRepository extends JpaRepository<ClinicalResourceVersion, UUID> {
  List<ClinicalResourceVersion> findByResourceIdOrderByVersionNumber(UUID resourceId);
  Optional<ClinicalResourceVersion> findByResourceIdAndVersionNumber(UUID resourceId, int versionNumber);
}
