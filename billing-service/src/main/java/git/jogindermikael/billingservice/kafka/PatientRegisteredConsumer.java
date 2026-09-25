package git.jogindermikael.billingservice.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import git.jogindermikael.billingservice.service.BillingAccountService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.UUID;
import git.jogindermikael.reliability.ReliableEventInbox;

@Component
public class PatientRegisteredConsumer {
    private final BillingAccountService service;
    private final ObjectMapper objectMapper;
    private final ReliableEventInbox inbox;
    public PatientRegisteredConsumer(BillingAccountService service, ObjectMapper objectMapper, ReliableEventInbox inbox) {
        this.service = service; this.objectMapper = objectMapper; this.inbox = inbox;
    }
    @KafkaListener(topics = "patient.events.v1", groupId = "billing-service")
    public void consume(byte[] value) throws Exception {
        JsonNode event = objectMapper.readTree(value);
        if (event.path("schemaVersion").asInt() != 1 || !"PATIENT_REGISTERED".equals(event.path("eventType").asText())) return;
        UUID eventId = UUID.fromString(event.path("eventId").asText());
        UUID patientId = UUID.fromString(event.path("patientId").asText());
        inbox.processOnce(eventId, "billing-service", "PATIENT_REGISTERED", value,
                () -> service.create(patientId, "patient-event:" + eventId));
    }
}
