package git.jogindermikael.analyticsservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final ObjectMapper objectMapper;

    public KafkaConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "patient.events.v1", groupId = "analytics-service")
    public void consumeEvent(byte[] event) {
        try {
            JsonNode envelope = objectMapper.readTree(event);
            if (envelope.path("schemaVersion").asInt() != 1) {
                log.warn("Ignoring unsupported patient event schema version {}",
                        envelope.path("schemaVersion").asText());
                return;
            }

            // ... perform any business related analytics
            log.info("Received patient event: [EventId={}, EventType={}, PatientId={}]",
                    envelope.path("eventId").asText(),
                    envelope.path("eventType").asText(),
                    envelope.path("patientId").asText());
        } catch (IOException exception) {
            log.error("Error deserializing patient event: {}", exception.getMessage());
        }
    }
}
