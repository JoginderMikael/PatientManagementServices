package git.jogindermikael.billingservice.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import git.jogindermikael.billingservice.service.BillingAccountService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class PatientRegisteredConsumer {
    private final BillingAccountService service;
    private final ObjectMapper objectMapper;
    public PatientRegisteredConsumer(BillingAccountService service, ObjectMapper objectMapper) {
        this.service = service; this.objectMapper = objectMapper;
    }
    @KafkaListener(topics = "patient.events.v1", groupId = "billing-service")
    public void consume(byte[] value) throws Exception {
        JsonNode event = objectMapper.readTree(value);
        if (event.path("schemaVersion").asInt() != 1 || !"PATIENT_REGISTERED".equals(event.path("eventType").asText())) return;
        UUID patientId = UUID.fromString(event.path("patientId").asText());
        service.create(patientId, "patient-event:" + event.path("eventId").asText());
    }
}
