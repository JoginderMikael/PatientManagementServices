package git.jogindermikael.notificationservice;

import static org.junit.jupiter.api.Assertions.*;

import git.jogindermikael.notificationservice.dto.NotificationDtos.*;
import git.jogindermikael.notificationservice.repository.NotificationRepository;
import git.jogindermikael.notificationservice.service.NotificationService;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(
    properties = {
      "spring.kafka.listener.auto-startup=false",
      "app.audit.enabled=false",
      "app.notifications.dispatch-delay-ms=3600000",
      "app.reliability.publish-initial-delay-ms=3600000"
    })
class Phase6NotificationOperationsTest {
  @Autowired NotificationService service;
  @Autowired NotificationRepository repository;

  @Test
  void enforcesOptOutButAllowsEssentialMfa() {
    UUID recipient = UUID.randomUUID();
    service.savePreference(
        new NotificationPreferenceRequest(
            recipient, "UTC", null, null, "en", true, Set.of("EMAIL")));

    assertThrows(
        ResponseStatusException.class,
        () ->
            service.send(
                new NotificationRequest(
                    recipient, "EMAIL", "synthetic@example.test", "MARKETING", "Synthetic", UUID.randomUUID())));

    assertEquals(
        "QUEUED",
        service
            .sendMfaCode(
                new MfaCodeRequest(recipient, "EMAIL", "synthetic@example.test", "123456"))
            .status());
  }

  @Test
  void rendersLocalizedTemplateAndDefersDuringQuietHours() {
    UUID recipient = UUID.randomUUID();
    service.savePreference(
        new NotificationPreferenceRequest(
            recipient,
            "UTC",
            LocalTime.of(0, 0),
            LocalTime.of(23, 59),
            "en-KE",
            false,
            Set.of("EMAIL")));
    String key = "APPOINTMENT_" + UUID.randomUUID();
    service.saveTemplate(
        new NotificationTemplateRequest(
            key, "en-KE", "EMAIL", "Reminder", "Hello {{name}}, appointment {{time}}"));

    Instant before = Instant.now();
    var message =
        service.sendTemplate(
            new TemplateNotificationRequest(
                recipient,
                "synthetic@example.test",
                key,
                null,
                "EMAIL",
                Map.of("name", "Patient", "time", "10:00"),
                UUID.randomUUID()));

    assertEquals("Hello Patient, appointment 10:00", message.body());
    assertTrue(message.getNextAttemptAt().isAfter(before));
  }

  @Test
  void exposesAndReplaysDeadLetteredDelivery() {
    var message =
        service.send(
            new NotificationRequest(
                UUID.randomUUID(),
                "SMS",
                "+254700000000",
                "TEST",
                "Synthetic",
                UUID.randomUUID()));
    message.markFailed("provider unavailable", 1);
    repository.saveAndFlush(message);

    assertTrue(service.deadLetters(0, 20).stream().anyMatch(item -> item.id().equals(message.id())));
    assertEquals("QUEUED", service.replay(message.id()).status());
  }
}
