package git.jogindermikael.patientportalservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import git.jogindermikael.reliability.ReliableEventInbox;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class PatientLifecycleConsumer {
  private final ObjectMapper mapper;
  private final JdbcTemplate jdbc;
  private final ReliableEventInbox inbox;

  public PatientLifecycleConsumer(ObjectMapper mapper, JdbcTemplate jdbc, ReliableEventInbox inbox) {
    this.mapper = mapper;
    this.jdbc = jdbc;
    this.inbox = inbox;
  }

  @KafkaListener(topics = "patient.events.v1", groupId = "patient-portal-service")
  public void consume(byte[] value) throws Exception {
    JsonNode event = mapper.readTree(value);
    if (event.path("schemaVersion").asInt() != 1) return;
    String eventType = event.path("eventType").asText();
    if (!eventType.equals("PATIENT_STATUS_CHANGED")
        && !eventType.equals("PATIENT_MERGED")
        && !eventType.equals("PATIENT_UNMERGED")
        && !eventType.equals("PATIENT_ARCHIVED")) return;
    String patientId = event.path("patientId").asText();
    String status = event.path("payload").path("status").asText();
    if (!patientId.isBlank() && !status.isBlank()) {
      String rawEventId = event.path("eventId").asText();
      UUID eventId =
          rawEventId.isBlank()
              ? UUID.nameUUIDFromBytes(
                  ("legacy-patient-event:" + new String(value, StandardCharsets.UTF_8))
                      .getBytes(StandardCharsets.UTF_8))
              : UUID.fromString(rawEventId);
      inbox.processOnce(eventId, "patient-portal-service", eventType, value,
          () -> jdbc.update(
              "UPDATE portal_identity SET patient_status=? WHERE patient_id=?",
              status,
              UUID.fromString(patientId)));
    }
  }
}
