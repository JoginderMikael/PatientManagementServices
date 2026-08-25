package git.jogindermikael.notificationservice.repository;

import git.jogindermikael.notificationservice.model.NotificationMessage;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class NotificationRepository {
    private final ConcurrentHashMap<UUID, NotificationMessage> messages = new ConcurrentHashMap<>();

    public NotificationMessage save(NotificationMessage message) {
        messages.put(message.id(), message);
        return message;
    }

    public Collection<NotificationMessage> findAll() {
        return messages.values();
    }
}
