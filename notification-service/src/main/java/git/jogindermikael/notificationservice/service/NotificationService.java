package git.jogindermikael.notificationservice.service;

import git.jogindermikael.notificationservice.dto.*;
import git.jogindermikael.notificationservice.mapper.NotificationMapper;
import git.jogindermikael.notificationservice.model.NotificationMessage;
import git.jogindermikael.notificationservice.model.NotificationPreference;
import git.jogindermikael.notificationservice.model.NotificationTemplate;
import git.jogindermikael.notificationservice.repository.NotificationPreferenceRepository;
import git.jogindermikael.notificationservice.repository.NotificationRepository;
import git.jogindermikael.notificationservice.repository.NotificationTemplateRepository;
import git.jogindermikael.reliability.DurableEventOutbox;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class NotificationService {
  private static final Set<String> CHANNELS = Set.of("EMAIL", "SMS", "PUSH");
  private final NotificationRepository repository;
  private final NotificationPreferenceRepository preferences;
  private final NotificationTemplateRepository templates;
  private final NotificationMapper mapper;
  private final DurableEventOutbox outbox;

  public NotificationService(
      NotificationRepository repository,
      NotificationPreferenceRepository preferences,
      NotificationTemplateRepository templates,
      NotificationMapper mapper,
      DurableEventOutbox outbox) {
    this.repository = repository;
    this.preferences = preferences;
    this.templates = templates;
    this.mapper = mapper;
    this.outbox = outbox;
  }

  public NotificationMessage send(NotificationRequest request) {
    return enqueue(mapper.fromNotificationRequest(request), false);
  }

  public NotificationMessage sendAppointmentReminder(AppointmentReminderRequest request) {
    return enqueue(mapper.fromAppointmentReminder(request), false);
  }

  public NotificationMessage sendBillAlert(BillAlertRequest request) {
    return enqueue(mapper.fromBillAlert(request), false);
  }

  public NotificationMessage sendMfaCode(MfaCodeRequest request) {
    return enqueue(mapper.fromMfaCode(request), true);
  }

  public NotificationMessage sendTemplate(TemplateNotificationRequest request) {
    String channel = validateChannel(request.channel());
    NotificationPreference preference = preferences.findById(request.recipientId()).orElse(null);
    String locale =
        request.locale() == null || request.locale().isBlank()
            ? preference == null ? "en" : preference.getLocale()
            : request.locale();
    NotificationTemplate template =
        templates
            .findByTemplateKeyAndLocaleAndChannelAndActiveTrue(
                request.templateKey(), locale, channel)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Active notification template not found"));
    NotificationMessage message =
        mapper.fromNotificationRequest(
            new NotificationRequest(
                request.recipientId(),
                channel,
                request.destination(),
                template.getTemplateKey(),
                template.render(request.variables()),
                request.correlationId()));
    return enqueue(message, false);
  }

  private NotificationMessage enqueue(NotificationMessage message, boolean essential) {
    String channel = validateChannel(message.channel());
    if (message.correlationId() != null) {
      NotificationMessage existing =
          repository.findByCorrelationIdAndTemplate(message.correlationId(), message.template()).orElse(null);
      if (existing != null) return existing;
    }
    NotificationPreference preference = preferences.findById(message.recipientId()).orElse(null);
    if (preference != null && !essential) {
      if (preference.isOptOut()) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Recipient has opted out");
      }
      if (!preference.channelSet().contains(channel)) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Channel is disabled by recipient");
      }
      message.scheduleAt(preference.nextAllowedDelivery(Instant.now()));
    }
    NotificationMessage saved = repository.save(message);
    outbox.append(
        "notification.events.v1",
        saved.id().toString(),
        Map.of(
            "eventId", UUID.randomUUID(),
            "schemaVersion", 1,
            "eventType", "NOTIFICATION_QUEUED",
            "occurredAt", Instant.now(),
            "source", "notification-service",
            "aggregateId", saved.id(),
            "recipientId", saved.recipientId(),
            "channel", saved.channel(),
            "template", saved.template()));
    return saved;
  }

  public NotificationPreference savePreference(NotificationPreferenceRequest request) {
    Set<String> channels =
        request.allowedChannels().stream()
            .map(this::validateChannel)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
    return preferences.save(
        new NotificationPreference(
            request.recipientId(),
            request.timezone(),
            request.quietStart(),
            request.quietEnd(),
            request.locale(),
            request.optOut(),
            channels));
  }

  @Transactional(readOnly = true)
  public NotificationPreference preference(UUID recipientId) {
    return preferences
        .findById(recipientId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Preference not found"));
  }

  public NotificationTemplate saveTemplate(NotificationTemplateRequest request) {
    validateChannel(request.channel());
    return templates.save(
        new NotificationTemplate(
            UUID.randomUUID(),
            request.templateKey(),
            request.locale(),
            request.channel(),
            request.subject(),
            request.body()));
  }

  public NotificationMessage applyCallback(DeliveryCallbackRequest request) {
    NotificationMessage message =
        repository
            .findByProviderMessageId(request.providerMessageId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
    message.applyProviderCallback(request.status(), request.detail());
    return message;
  }

  public NotificationMessage replay(UUID id) {
    NotificationMessage message =
        repository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
    try {
      message.replay();
    } catch (IllegalStateException exception) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
    }
    return message;
  }

  @Transactional(readOnly = true)
  public Page<NotificationMessage> deadLetters(int page, int size) {
    return repository.findByStatus(
        "DEAD_LETTER", PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 200))));
  }

  @Transactional(readOnly = true)
  public List<NotificationMessage> list(UUID recipientId) {
    return (recipientId == null
            ? repository.findAll()
            : repository.findByRecipientIdOrderByCreatedAt(recipientId))
        .stream()
        .sorted(Comparator.comparing(NotificationMessage::createdAt))
        .toList();
  }

  private String validateChannel(String channel) {
    String normalized = channel.toUpperCase(Locale.ROOT);
    if (!CHANNELS.contains(normalized)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported notification channel");
    }
    return normalized;
  }
}
