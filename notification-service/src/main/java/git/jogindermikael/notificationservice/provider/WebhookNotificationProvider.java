package git.jogindermikael.notificationservice.provider;

import git.jogindermikael.notificationservice.model.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;
import java.util.UUID;

@Component
public class WebhookNotificationProvider implements NotificationProvider {
    private static final Logger log = LoggerFactory.getLogger(WebhookNotificationProvider.class);
    private final String endpoint;
    private final String bearerToken;
    private final RestClient client = RestClient.create();

    public WebhookNotificationProvider(@Value("${app.notifications.provider.webhook-url:}") String endpoint,
            @Value("${app.notifications.provider.bearer-token:}") String bearerToken) {
        this.endpoint = endpoint;
        this.bearerToken = bearerToken;
    }

    @Override
    public String deliver(NotificationMessage message) {
        if (endpoint.isBlank()) {
            String localId = "local-" + UUID.randomUUID();
            log.info("Local notification provider accepted {} message {} as {}", message.getChannel(), message.getId(),
                    localId);
            return localId;
        }
        Map<String, Object> payload = Map.of("id", message.getId(), "channel", message.getChannel(), "destination",
                message.getDestination(), "template", message.getTemplate(), "body", message.getBody());
        RestClient.RequestBodySpec request = client.post().uri(endpoint).contentType(MediaType.APPLICATION_JSON);
        if (!bearerToken.isBlank())
            request.header("Authorization", "Bearer " + bearerToken);
        String providerId = request.body(payload).retrieve().body(String.class);
        return providerId == null || providerId.isBlank() ? "provider-" + message.getId() : providerId;
    }
}
