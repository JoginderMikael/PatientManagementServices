package git.jogindermikael.ehrservice.repository;

import git.jogindermikael.ehrservice.model.ClinicalResource;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicalResourceRepository extends JpaRepository<ClinicalResource, UUID> {
  Page<ClinicalResource> findByPatientId(UUID patientId, Pageable pageable);
  Page<ClinicalResource> findByPatientIdAndResourceType(UUID patientId, String resourceType, Pageable pageable);
}
