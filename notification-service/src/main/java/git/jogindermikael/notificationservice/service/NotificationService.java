package git.jogindermikael.notificationservice.service;

import git.jogindermikael.notificationservice.dto.NotificationDtos.*;
import git.jogindermikael.notificationservice.mapper.NotificationMapper;
import git.jogindermikael.notificationservice.model.NotificationMessage;
import git.jogindermikael.notificationservice.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepository repository;
    private final NotificationMapper mapper;

    public NotificationService(NotificationRepository repository, NotificationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public NotificationMessage send(NotificationRequest request) {
        validateChannel(request.channel());
        return repository.save(mapper.fromNotificationRequest(request));
    }

    public NotificationMessage sendAppointmentReminder(AppointmentReminderRequest request) {
        validateChannel(request.channel());
        return repository.save(mapper.fromAppointmentReminder(request));
    }

    public NotificationMessage sendBillAlert(BillAlertRequest request) {
        validateChannel(request.channel());
        return repository.save(mapper.fromBillAlert(request));
    }

    public NotificationMessage sendMfaCode(MfaCodeRequest request) {
        validateChannel(request.channel());
        return repository.save(mapper.fromMfaCode(request));
    }

    public List<NotificationMessage> list(UUID recipientId) {
        return (recipientId == null ? repository.findAll() : repository.findByRecipientIdOrderByCreatedAt(recipientId))
                .stream()
                .sorted(Comparator.comparing(NotificationMessage::createdAt))
                .toList();
    }

    private void validateChannel(String channel) {
        if (!java.util.Set.of("EMAIL", "SMS", "PUSH").contains(channel.toUpperCase())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Unsupported notification channel");
        }
    }
}
