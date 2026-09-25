package git.jogindermikael.notificationservice.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import git.jogindermikael.notificationservice.dto.NotificationRequest;
import git.jogindermikael.notificationservice.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.UUID;
import git.jogindermikael.reliability.ReliableEventInbox;

@Component
public class NotificationRequestConsumer {
    private final NotificationService service;
    private final ObjectMapper objectMapper;
    private final ReliableEventInbox inbox;

    public NotificationRequestConsumer(NotificationService service, ObjectMapper objectMapper, ReliableEventInbox inbox) {
        this.service = service;
        this.objectMapper = objectMapper;
        this.inbox = inbox;
    }

    @KafkaListener(topics = "notification.requests.v1", groupId = "notification-service")
    public void consume(byte[] value) throws Exception {
        JsonNode event = objectMapper.readTree(value);
        if (event.path("schemaVersion").asInt() != 1)
            return;
        JsonNode payload = event.path("payload");
        UUID eventId = UUID.fromString(event.path("eventId").asText());
        inbox.processOnce(eventId, "notification-service", event.path("eventType").asText(), value,
                () -> service.send(new NotificationRequest(UUID.fromString(payload.path("recipientId").asText()),
                        payload.path("channel").asText(), payload.path("destination").asText(),
                        payload.path("template").asText(), payload.path("body").asText(), eventId)));
    }
}
