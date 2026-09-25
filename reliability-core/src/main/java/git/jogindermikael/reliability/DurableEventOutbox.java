package git.jogindermikael.reliability;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

public class DurableEventOutbox {
  private final JdbcTemplate jdbc;
  private final ObjectMapper mapper;

  public DurableEventOutbox(JdbcTemplate jdbc, ObjectMapper mapper) {
    this.jdbc = jdbc;
    this.mapper = mapper;
  }

  public UUID append(String topic, String eventKey, Object payload) {
    try {
      return appendRaw(topic, eventKey, mapper.writeValueAsString(payload));
    } catch (JsonProcessingException exception) {
      throw new IllegalArgumentException("Event payload is not serializable", exception);
    }
  }

  public UUID appendRaw(String topic, String eventKey, String payload) {
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    jdbc.update(
        "INSERT INTO reliable_event_outbox "
            + "(id,topic,event_key,payload,status,attempts,replay_count,available_at,created_at) "
            + "VALUES (?,?,?,?, 'PENDING',0,0,?,?)",
        id,
        requireText(topic, "topic"),
        requireText(eventKey, "eventKey"),
        requireText(payload, "payload"),
        Timestamp.from(now),
        Timestamp.from(now));
    return id;
  }

  private String requireText(String value, String field) {
    if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
    return value;
  }
}
