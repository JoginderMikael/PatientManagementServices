package git.jogindermikael.notificationservice.service;

import git.jogindermikael.notificationservice.model.NotificationMessage;
import git.jogindermikael.notificationservice.provider.NotificationProvider;
import git.jogindermikael.notificationservice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Component
public class NotificationDispatcher {
    private final NotificationRepository repository;
    private final NotificationProvider provider;
    private final int maxAttempts;

    public NotificationDispatcher(NotificationRepository repository, NotificationProvider provider,
            @Value("${app.notifications.max-attempts:5}") int maxAttempts) {
        this.repository = repository;
        this.provider = provider;
        this.maxAttempts = maxAttempts;
    }

    @Scheduled(fixedDelayString = "${app.notifications.dispatch-delay-ms:1000}")
    @Transactional
    public void dispatch() {
        for (NotificationMessage message : repository.findTop50ByStatusInAndNextAttemptAtLessThanEqualOrderByCreatedAt(
                List.of("QUEUED", "FAILED"), Instant.now())) {
            try {
                message.markSent(provider.deliver(message));
            } catch (Exception exception) {
                message.markFailed(exception.getMessage(), maxAttempts);
            }
        }
    }
}
