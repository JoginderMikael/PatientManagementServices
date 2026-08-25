package git.jogindermikael.auditcomplianceservice.mapper;

import git.jogindermikael.auditcomplianceservice.dto.AuditEventRequest;
import git.jogindermikael.auditcomplianceservice.model.AuditEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class AuditEventMapper {
    public AuditEvent toEvent(AuditEventRequest request) {
        return new AuditEvent(UUID.randomUUID(), request.actorId(), request.actorRole(), request.action(), request.patientId(), request.resourceType(), request.resourceId(), request.sourceService(), request.reason(), Instant.now());
    }
}
