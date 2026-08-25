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

    public NotificationMessage send(NotificationRequest request) { return repository.save(mapper.fromNotificationRequest(request)); }
    public NotificationMessage sendAppointmentReminder(AppointmentReminderRequest request) { return repository.save(mapper.fromAppointmentReminder(request)); }
    public NotificationMessage sendBillAlert(BillAlertRequest request) { return repository.save(mapper.fromBillAlert(request)); }
    public NotificationMessage sendMfaCode(MfaCodeRequest request) { return repository.save(mapper.fromMfaCode(request)); }

    public List<NotificationMessage> list(UUID recipientId) {
        return repository.findAll().stream()
                .filter(message -> recipientId == null || message.recipientId().equals(recipientId))
                .sorted(Comparator.comparing(NotificationMessage::createdAt))
                .toList();
    }
}
