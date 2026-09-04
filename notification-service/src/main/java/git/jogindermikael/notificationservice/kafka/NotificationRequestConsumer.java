package git.jogindermikael.notificationservice.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import git.jogindermikael.notificationservice.dto.NotificationDtos.NotificationRequest;
import git.jogindermikael.notificationservice.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class NotificationRequestConsumer {
    private final NotificationService service;
    private final ObjectMapper objectMapper;

    public NotificationRequestConsumer(NotificationService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "notification.requests.v1", groupId = "notification-service")
    public void consume(byte[] value) throws Exception {
        JsonNode event = objectMapper.readTree(value);
        if (event.path("schemaVersion").asInt() != 1)
            return;
        JsonNode payload = event.path("payload");
        service.send(new NotificationRequest(UUID.fromString(payload.path("recipientId").asText()),
                payload.path("channel").asText(), payload.path("destination").asText(),
                payload.path("template").asText(), payload.path("body").asText(),
                UUID.fromString(event.path("eventId").asText())));
    }
}
