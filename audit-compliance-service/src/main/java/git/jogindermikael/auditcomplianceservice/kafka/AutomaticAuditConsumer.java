package git.jogindermikael.auditcomplianceservice.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import git.jogindermikael.auditcomplianceservice.service.AuditComplianceService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.UUID;
import git.jogindermikael.reliability.ReliableEventInbox;

@Component
public class AutomaticAuditConsumer {
    private final AuditComplianceService service;
    private final ObjectMapper mapper;
    private final ReliableEventInbox inbox;

    public AutomaticAuditConsumer(AuditComplianceService service, ObjectMapper mapper, ReliableEventInbox inbox) {
        this.service = service;
        this.mapper = mapper;
        this.inbox = inbox;
    }

    @KafkaListener(topics = { "patient.events.v1", "billing.events.v1", "appointment.events.v1", "ehr.events.v1",
            "notification.events.v1", "audit.events.v1" }, groupId = "audit-compliance-service")
    public void consume(byte[] value) throws Exception {
        JsonNode event = mapper.readTree(value);
        if (event.path("schemaVersion").asInt() != 1)
            return;
        UUID eventId = uuid(event, "eventId");
        inbox.processOnce(eventId, "audit-compliance-service", text(event, "eventType", "UNKNOWN"), value,
                () -> service.append(eventId, text(event, "actorId", "system"), text(event, "actorRole", "SYSTEM"),
                        text(event, "eventType", "UNKNOWN"), nullableUuid(event, "patientId"),
                        text(event, "aggregateType", "HTTP"), nullableUuid(event, "aggregateId"),
                        text(event, "source", "unknown-service"), text(event, "outcome", "SUCCESS"),
                        text(event, "reason", null), text(event, "endpoint", null), text(event, "requestId", null),
                        text(event, "correlationId", null), Instant.parse(text(event, "occurredAt", Instant.now().toString()))));
    }

    private UUID uuid(JsonNode node, String field) {
        return UUID.fromString(node.path(field).asText());
    }

    private UUID nullableUuid(JsonNode node, String field) {
        String value = node.path(field).asText();
        try {
            return value.isBlank() ? null : UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String text(JsonNode node, String field, String fallback) {
        String value = node.path(field).asText();
        return value.isBlank() ? fallback : value;
    }
}
