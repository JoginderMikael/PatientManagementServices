package git.jogindermikael.reliability;

import git.jogindermikael.reliability.dto.OutboxRecord;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;

public class ReliableOutboxPublisher {
  private static final Logger log = LoggerFactory.getLogger(ReliableOutboxPublisher.class);
  private final JdbcTemplate jdbc;
  private final KafkaTemplate<String, byte[]> kafka;
  private final ReliableOutboxOperations operations;
  private final int batchSize;
  private final int maxAttempts;
  private final long sendTimeoutSeconds;
  private final Duration claimLease;
  private final int retentionDays;

  public ReliableOutboxPublisher(
      JdbcTemplate jdbc,
      KafkaTemplate<String, byte[]> kafka,
      ReliableOutboxOperations operations,
      int batchSize,
      int maxAttempts,
      long sendTimeoutSeconds,
      Duration claimLease,
      int retentionDays) {
    this.jdbc = jdbc;
    this.kafka = kafka;
    this.operations = operations;
    this.batchSize = batchSize;
    this.maxAttempts = maxAttempts;
    this.sendTimeoutSeconds = sendTimeoutSeconds;
    this.claimLease = claimLease;
    this.retentionDays = retentionDays;
  }

  @Scheduled(
      fixedDelayString = "${app.reliability.publish-delay-ms:1000}",
      initialDelayString = "${app.reliability.publish-initial-delay-ms:60000}")
  public void publishPending() {
    operations.recoverExpiredClaims(claimLease);
    List<UUID> ids =
        jdbc.queryForList(
            "SELECT id FROM reliable_event_outbox "
                + "WHERE status IN ('PENDING','FAILED') AND available_at<=CURRENT_TIMESTAMP "
                + "ORDER BY created_at LIMIT ?",
            UUID.class,
            batchSize);
    ids.forEach(this::publishOne);
  }

  void publishOne(UUID id) {
    int claimed =
        jdbc.update(
            "UPDATE reliable_event_outbox SET status='PROCESSING', claimed_at=CURRENT_TIMESTAMP "
                + "WHERE id=? AND status IN ('PENDING','FAILED') AND available_at<=CURRENT_TIMESTAMP",
            id);
    if (claimed == 0) return;

    OutboxRecord event =
        jdbc.queryForObject(
            "SELECT * FROM reliable_event_outbox WHERE id=?",
            (result, row) ->
                new OutboxRecord(
                    result.getObject("id", UUID.class),
                    result.getString("topic"),
                    result.getString("event_key"),
                    result.getString("payload"),
                    result.getString("status"),
                    result.getInt("attempts"),
                    result.getInt("replay_count"),
                    result.getTimestamp("available_at").toInstant(),
                    result.getTimestamp("created_at").toInstant(),
                    null,
                    null,
                    result.getString("last_error")),
            id);
    try {
      kafka
          .send(
              event.topic(), event.eventKey(), event.payload().getBytes(StandardCharsets.UTF_8))
          .get(sendTimeoutSeconds, TimeUnit.SECONDS);
      jdbc.update(
          "UPDATE reliable_event_outbox SET status='PUBLISHED', attempts=attempts+1, "
              + "published_at=CURRENT_TIMESTAMP, claimed_at=NULL, last_error=NULL WHERE id=?",
          id);
    } catch (Exception exception) {
      int nextAttempt = event.attempts() + 1;
      boolean deadLetter = nextAttempt >= maxAttempts;
      Instant available =
          Instant.now().plus((long) Math.pow(2, Math.min(nextAttempt, 10)), ChronoUnit.MINUTES);
      jdbc.update(
          "UPDATE reliable_event_outbox SET status=?, attempts=?, available_at=?, claimed_at=NULL, "
              + "dead_lettered_at=?, last_error=? WHERE id=?",
          deadLetter ? "DEAD_LETTER" : "FAILED",
          nextAttempt,
          Timestamp.from(available),
          deadLetter ? Timestamp.from(Instant.now()) : null,
          abbreviate(exception.getMessage()),
          id);
      log.warn("Reliable outbox event {} delivery attempt {} failed", id, nextAttempt);
    }
  }

  @Scheduled(cron = "${app.reliability.retention-cron:0 30 2 * * *}")
  public void purgePublished() {
    jdbc.update(
        "DELETE FROM reliable_event_outbox WHERE status='PUBLISHED' AND published_at<?",
        Timestamp.from(Instant.now().minus(retentionDays, ChronoUnit.DAYS)));
  }

  private String abbreviate(String value) {
    String message = value == null ? "Kafka delivery failed" : value;
    return message.substring(0, Math.min(message.length(), 2000));
  }
}
