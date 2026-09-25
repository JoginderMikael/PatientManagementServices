package git.jogindermikael.ehrservice.repository;

import git.jogindermikael.ehrservice.model.ClinicalProvenance;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicalProvenanceRepository extends JpaRepository<ClinicalProvenance, UUID> {
  List<ClinicalProvenance> findByResourceIdOrderByVersionNumber(UUID resourceId);
}
