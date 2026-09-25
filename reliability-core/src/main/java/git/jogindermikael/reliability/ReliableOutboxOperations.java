package git.jogindermikael.reliability;

import git.jogindermikael.reliability.dto.OutboxRecord;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

public class ReliableOutboxOperations {
  private static final Set<String> STATUSES =
      Set.of("PENDING", "PROCESSING", "FAILED", "DEAD_LETTER", "PUBLISHED");
  private final JdbcTemplate jdbc;

  public ReliableOutboxOperations(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<OutboxRecord> list(String requestedStatus, int offset, int limit) {
    int safeOffset = Math.max(0, offset);
    int safeLimit = Math.max(1, Math.min(limit, 200));
    if (requestedStatus == null || requestedStatus.isBlank()) {
      return jdbc.query(
          "SELECT * FROM reliable_event_outbox ORDER BY created_at DESC LIMIT ? OFFSET ?",
          this::map,
          safeLimit,
          safeOffset);
    }
    String status = requestedStatus.toUpperCase(Locale.ROOT);
    if (!STATUSES.contains(status)) throw new IllegalArgumentException("Unsupported outbox status");
    return jdbc.query(
        "SELECT * FROM reliable_event_outbox WHERE status=? ORDER BY created_at DESC LIMIT ? OFFSET ?",
        this::map,
        status,
        safeLimit,
        safeOffset);
  }

  public boolean replay(UUID id) {
    return jdbc.update(
            "UPDATE reliable_event_outbox SET status='PENDING', attempts=0, replay_count=replay_count+1, "
                + "available_at=CURRENT_TIMESTAMP, claimed_at=NULL, dead_lettered_at=NULL, last_error=NULL "
                + "WHERE id=? AND status IN ('FAILED','DEAD_LETTER')",
            id)
        == 1;
  }

  public int recoverExpiredClaims(Duration lease) {
    Instant expired = Instant.now().minus(lease);
    return jdbc.update(
        "UPDATE reliable_event_outbox SET status='FAILED', claimed_at=NULL, "
            + "available_at=CURRENT_TIMESTAMP, last_error='Publisher lease expired' "
            + "WHERE status='PROCESSING' AND claimed_at<?",
        Timestamp.from(expired));
  }

  public int pendingCount() {
    Integer count =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM reliable_event_outbox WHERE status IN ('PENDING','FAILED','PROCESSING')",
            Integer.class);
    return count == null ? 0 : count;
  }

  private OutboxRecord map(ResultSet result, int row) throws SQLException {
    return new OutboxRecord(
        result.getObject("id", UUID.class),
        result.getString("topic"),
        result.getString("event_key"),
        result.getString("payload"),
        result.getString("status"),
        result.getInt("attempts"),
        result.getInt("replay_count"),
        instant(result, "available_at"),
        instant(result, "created_at"),
        instant(result, "published_at"),
        instant(result, "dead_lettered_at"),
        result.getString("last_error"));
  }

  private Instant instant(ResultSet result, String column) throws SQLException {
    Timestamp value = result.getTimestamp(column);
    return value == null ? null : value.toInstant();
  }
}
