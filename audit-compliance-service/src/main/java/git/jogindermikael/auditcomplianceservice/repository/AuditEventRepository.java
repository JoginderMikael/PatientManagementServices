package git.jogindermikael.auditcomplianceservice.repository;

import git.jogindermikael.auditcomplianceservice.model.AuditEvent;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AuditEventRepository {
    private final ConcurrentHashMap<UUID, AuditEvent> events = new ConcurrentHashMap<>();

    public AuditEvent save(AuditEvent event) {
        events.put(event.id(), event);
        return event;
    }

    public Collection<AuditEvent> findAll() {
        return events.values();
    }
}
