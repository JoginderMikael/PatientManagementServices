package git.jogindermikael.patientservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import git.jogindermikael.patientservice.model.OutboxEvent;
import git.jogindermikael.patientservice.model.Patient;
import git.jogindermikael.patientservice.repository.OutboxEventRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PatientOutboxService {
    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;
    public PatientOutboxService(OutboxEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository; this.objectMapper = objectMapper;
    }
    public void append(String eventType, Patient patient) {
        UUID eventId = UUID.randomUUID();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventId", eventId);
        event.put("schemaVersion", 1);
        event.put("eventType", eventType);
        event.put("occurredAt", Instant.now());
        event.put("source", "patient-service");
        event.put("aggregateType", "PATIENT");
        event.put("aggregateId", patient.getId());
        event.put("patientId", patient.getId());
        event.put("actorId", authentication == null ? "system" : authentication.getName());
        event.put("actorRole", authentication == null ? "SYSTEM" : authentication.getAuthorities().stream().findFirst().map(Object::toString).orElse("AUTHENTICATED"));
        event.put("correlationId", eventId);
        event.put("payload", Map.of("patientId", patient.getId().toString(), "mrn", patient.getMrn(), "status", patient.getStatus().name()));
        try {
            repository.save(new OutboxEvent(eventId, "patient.events.v1", patient.getId().toString(), objectMapper.writeValueAsString(event)));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize patient event", exception);
        }
    }
}
