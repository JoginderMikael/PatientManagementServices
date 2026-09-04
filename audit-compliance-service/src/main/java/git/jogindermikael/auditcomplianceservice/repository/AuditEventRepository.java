package git.jogindermikael.auditcomplianceservice.repository;

import git.jogindermikael.auditcomplianceservice.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
    Optional<AuditEvent> findBySourceEventId(UUID sourceEventId);

    Optional<AuditEvent> findTopByOrderByOccurredAtDesc();

    List<AuditEvent> findByPatientIdOrderByOccurredAt(UUID patientId);

    List<AuditEvent> findByActorIdOrderByOccurredAt(String actorId);

    List<AuditEvent> findByPatientIdAndActorIdOrderByOccurredAt(UUID patientId, String actorId);
}
