package git.jogindermikael.notificationservice.repository;

import git.jogindermikael.notificationservice.model.NotificationTemplate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTemplateRepository
    extends JpaRepository<NotificationTemplate, UUID> {
  Optional<NotificationTemplate> findByTemplateKeyAndLocaleAndChannelAndActiveTrue(
      String templateKey, String locale, String channel);
}
