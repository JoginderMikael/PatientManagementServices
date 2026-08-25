package git.jogindermikael.auditcomplianceservice.service;

import git.jogindermikael.auditcomplianceservice.dto.AuditEventRequest;
import git.jogindermikael.auditcomplianceservice.mapper.AuditEventMapper;
import git.jogindermikael.auditcomplianceservice.model.AuditEvent;
import git.jogindermikael.auditcomplianceservice.repository.AuditEventRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class AuditComplianceService {
    private final AuditEventRepository repository;
    private final AuditEventMapper mapper;

    public AuditComplianceService(AuditEventRepository repository, AuditEventMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public AuditEvent recordEvent(AuditEventRequest request) {
        return repository.save(mapper.toEvent(request));
    }

    public List<AuditEvent> searchEvents(UUID patientId, UUID actorId) {
        return repository.findAll().stream()
                .filter(event -> patientId == null || event.patientId().equals(patientId))
                .filter(event -> actorId == null || event.actorId().equals(actorId))
                .sorted(Comparator.comparing(AuditEvent::occurredAt))
                .toList();
    }

    public List<AuditEvent> patientRecordAccess(UUID patientId) {
        return repository.findAll().stream()
                .filter(event -> event.patientId().equals(patientId))
                .filter(event -> event.action().contains("VIEW") || event.action().contains("READ") || event.action().contains("UPDATE"))
                .sorted(Comparator.comparing(AuditEvent::occurredAt))
                .toList();
    }
}
