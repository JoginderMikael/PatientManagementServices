package git.jogindermikael.ehrservice.repository;

import git.jogindermikael.ehrservice.model.TerminologyCode;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerminologyCodeRepository extends JpaRepository<TerminologyCode, UUID> {
  Optional<TerminologyCode> findBySystemUriAndCode(String systemUri, String code);
}
