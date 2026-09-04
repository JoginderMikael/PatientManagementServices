package git.jogindermikael.notificationservice.provider;

import git.jogindermikael.notificationservice.model.NotificationMessage;

public interface NotificationProvider {
    String deliver(NotificationMessage message);
}
