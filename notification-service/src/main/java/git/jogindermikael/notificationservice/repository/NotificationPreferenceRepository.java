package git.jogindermikael.notificationservice.repository;

import git.jogindermikael.notificationservice.model.NotificationPreference;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceRepository
    extends JpaRepository<NotificationPreference, UUID> {}
