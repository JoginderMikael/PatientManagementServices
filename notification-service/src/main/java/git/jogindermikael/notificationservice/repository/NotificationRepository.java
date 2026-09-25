package git.jogindermikael.notificationservice.repository;

import git.jogindermikael.notificationservice.model.NotificationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepository extends JpaRepository<NotificationMessage, UUID> {
    List<NotificationMessage> findByRecipientIdOrderByCreatedAt(UUID recipientId);

    List<NotificationMessage> findTop50ByStatusInAndNextAttemptAtLessThanEqualOrderByCreatedAt(
            Collection<String> statuses, Instant now);

    Optional<NotificationMessage> findByCorrelationIdAndTemplate(UUID correlationId, String template);

    Optional<NotificationMessage> findByProviderMessageId(String providerMessageId);

    Page<NotificationMessage> findByStatus(String status, Pageable pageable);
}
