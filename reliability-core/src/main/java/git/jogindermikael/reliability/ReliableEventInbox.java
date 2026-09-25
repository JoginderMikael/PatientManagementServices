package git.jogindermikael.reliability;

import git.jogindermikael.reliability.dto.InboxRecord;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class ReliableEventInbox {
  private final JdbcTemplate jdbc;
  private final TransactionTemplate transactions;

  public ReliableEventInbox(JdbcTemplate jdbc, PlatformTransactionManager transactionManager) {
    this.jdbc = jdbc;
    this.transactions = new TransactionTemplate(transactionManager);
  }

  public boolean processOnce(
      UUID eventId, String consumer, String eventType, byte[] payload, Runnable handler) {
    if (!claim(eventId, consumer, eventType, payload)) return false;
    try {
      transactions.executeWithoutResult(
          ignored -> {
            handler.run();
            jdbc.update(
                "UPDATE reliable_event_inbox SET status='PROCESSED', processed_at=CURRENT_TIMESTAMP, "
                    + "claimed_at=NULL, last_error=NULL WHERE consumer_name=? AND event_id=?",
                consumer,
                eventId);
          });
      return true;
    } catch (RuntimeException exception) {
      jdbc.update(
          "UPDATE reliable_event_inbox SET status='FAILED', claimed_at=NULL, last_error=? "
              + "WHERE consumer_name=? AND event_id=?",
          abbreviate(exception.getMessage()),
          consumer,
          eventId);
      throw exception;
    }
  }

  private boolean claim(
      UUID eventId, String consumer, String eventType, byte[] payload) {
    int reclaimed =
        transactions.execute(
            ignored ->
                jdbc.update(
                    "UPDATE reliable_event_inbox SET status='PROCESSING', attempts=attempts+1, "
                        + "claimed_at=CURRENT_TIMESTAMP, last_error=NULL "
                        + "WHERE consumer_name=? AND event_id=? AND status='FAILED'",
                    consumer,
                    eventId));
    if (reclaimed == 1) return true;
    try {
      return Boolean.TRUE.equals(
          transactions.execute(
              ignored -> {
                jdbc.update(
                    "INSERT INTO reliable_event_inbox "
                        + "(id,consumer_name,event_id,event_type,payload_hash,status,attempts,received_at,claimed_at) "
                        + "VALUES (?,?,?,?,?,'PROCESSING',1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",
                    UUID.randomUUID(),
                    consumer,
                    eventId,
                    eventType,
                    sha256(payload));
                return true;
              }));
    } catch (DuplicateKeyException duplicate) {
      return false;
    }
  }

  public List<InboxRecord> list(String status, int offset, int limit) {
    int safeOffset = Math.max(0, offset);
    int safeLimit = Math.max(1, Math.min(limit, 200));
    if (status == null || status.isBlank()) {
      return jdbc.query(
          "SELECT * FROM reliable_event_inbox ORDER BY received_at DESC LIMIT ? OFFSET ?",
          (result, row) ->
              new InboxRecord(
                  result.getObject("event_id", UUID.class),
                  result.getString("consumer_name"),
                  result.getString("event_type"),
                  result.getString("status"),
                  result.getInt("attempts"),
                  result.getString("payload_hash"),
                  instant(result.getTimestamp("received_at")),
                  instant(result.getTimestamp("processed_at")),
                  result.getString("last_error")),
          safeLimit,
          safeOffset);
    }
    String normalized = status.toUpperCase();
    if (!java.util.Set.of("PROCESSING", "PROCESSED", "FAILED").contains(normalized)) {
      throw new IllegalArgumentException("Unsupported inbox status");
    }
    return jdbc.query(
        "SELECT * FROM reliable_event_inbox WHERE status=? ORDER BY received_at DESC LIMIT ? OFFSET ?",
        (result, row) ->
            new InboxRecord(
                result.getObject("event_id", UUID.class),
                result.getString("consumer_name"),
                result.getString("event_type"),
                result.getString("status"),
                result.getInt("attempts"),
                result.getString("payload_hash"),
                instant(result.getTimestamp("received_at")),
                instant(result.getTimestamp("processed_at")),
                result.getString("last_error")),
        normalized,
        safeLimit,
        safeOffset);
  }

  public int recoverExpiredClaims(Duration lease) {
    return jdbc.update(
        "UPDATE reliable_event_inbox SET status='FAILED', claimed_at=NULL, "
            + "last_error='Consumer lease expired' WHERE status='PROCESSING' AND claimed_at<?",
        Timestamp.from(Instant.now().minus(lease)));
  }

  private Instant instant(Timestamp value) {
    return value == null ? null : value.toInstant();
  }

  private String sha256(byte[] payload) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(payload));
    } catch (NoSuchAlgorithmException impossible) {
      throw new IllegalStateException("SHA-256 is unavailable", impossible);
    }
  }

  private String abbreviate(String value) {
    String message = value == null ? "Consumer processing failed" : value;
    return message.substring(0, Math.min(message.length(), 2000));
  }

}
