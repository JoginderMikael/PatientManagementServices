package git.jogindermikael.auditcomplianceservice.service;

import git.jogindermikael.auditcomplianceservice.dto.AuditEventRequest;
import git.jogindermikael.auditcomplianceservice.model.AuditEvent;
import git.jogindermikael.auditcomplianceservice.repository.AuditEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class AuditComplianceService {
    private static final String GENESIS_HASH="0".repeat(64);
    private final AuditEventRepository repository;
    public AuditComplianceService(AuditEventRepository repository){this.repository=repository;}

    @Transactional
    public AuditEvent recordEvent(AuditEventRequest request){
        return append(UUID.randomUUID(),request.actorId().toString(),request.actorRole(),request.action(),request.patientId(),request.resourceType(),request.resourceId(),request.sourceService(),"SUCCESS",request.reason(),null,null,null,Instant.now());
    }

    @Transactional
    public AuditEvent append(UUID sourceEventId,String actorId,String actorRole,String action,UUID patientId,String resourceType,UUID resourceId,String sourceService,String outcome,String reason,String endpoint,String requestId,String correlationId,Instant occurredAt){
        AuditEvent existing=repository.findBySourceEventId(sourceEventId).orElse(null);if(existing!=null)return existing;
        String previous=repository.findTopByOrderByOccurredAtDesc().map(AuditEvent::getEventHash).orElse(GENESIS_HASH);
        Instant timestamp=occurredAt==null?Instant.now():occurredAt;
        String canonical=String.join("|",previous,sourceEventId.toString(),safe(actorId),safe(action),safe(patientId),safe(resourceType),safe(resourceId),safe(sourceService),safe(outcome),timestamp.toString());
        String hash=sha256(canonical);
        return repository.save(new AuditEvent(UUID.randomUUID(),sourceEventId,safe(actorId),safe(actorRole),safe(action),patientId,safe(resourceType),resourceId,safe(sourceService),safe(outcome),reason,endpoint,requestId,correlationId,timestamp,previous,hash));
    }

    @Transactional(readOnly=true)
    public List<AuditEvent> searchEvents(UUID patientId,UUID actorId){
        if(patientId!=null&&actorId!=null)return repository.findByPatientIdAndActorIdOrderByOccurredAt(patientId,actorId.toString());
        if(patientId!=null)return repository.findByPatientIdOrderByOccurredAt(patientId);
        if(actorId!=null)return repository.findByActorIdOrderByOccurredAt(actorId.toString());
        return repository.findAll().stream().sorted(java.util.Comparator.comparing(AuditEvent::getOccurredAt)).toList();
    }

    @Transactional(readOnly=true)
    public List<AuditEvent> patientRecordAccess(UUID patientId){return repository.findByPatientIdOrderByOccurredAt(patientId).stream().filter(event->event.getAction().contains("VIEW")||event.getAction().contains("READ")||event.getAction().contains("GET")||event.getAction().contains("UPDATE")).toList();}

    private String sha256(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception exception){throw new IllegalStateException("SHA-256 is unavailable",exception);}}
    private String safe(Object value){return value==null?"":value.toString();}
}
