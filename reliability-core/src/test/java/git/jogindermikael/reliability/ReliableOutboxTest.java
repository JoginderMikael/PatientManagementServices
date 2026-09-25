package git.jogindermikael.reliability;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

class ReliableOutboxTest {
  private JdbcTemplate jdbc;
  private DurableEventOutbox outbox;
  private ReliableOutboxOperations operations;

  @BeforeEach
  void setUp() {
    JdbcDataSource dataSource = new JdbcDataSource();
    dataSource.setURL(
        "jdbc:h2:mem:reliability-"
            + UUID.randomUUID()
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
    jdbc = new JdbcTemplate(dataSource);
    jdbc.execute(
        "CREATE TABLE reliable_event_outbox ("
            + "id UUID PRIMARY KEY, topic VARCHAR(100) NOT NULL, event_key VARCHAR(200) NOT NULL,"
            + "payload TEXT NOT NULL, status VARCHAR(24) NOT NULL, attempts INTEGER NOT NULL,"
            + "replay_count INTEGER NOT NULL, available_at TIMESTAMP WITH TIME ZONE NOT NULL,"
            + "created_at TIMESTAMP WITH TIME ZONE NOT NULL, claimed_at TIMESTAMP WITH TIME ZONE,"
            + "published_at TIMESTAMP WITH TIME ZONE, dead_lettered_at TIMESTAMP WITH TIME ZONE,"
            + "last_error TEXT)");
    jdbc.execute(
        "CREATE TABLE reliable_event_inbox ("
            + "id UUID PRIMARY KEY, consumer_name VARCHAR(100) NOT NULL, event_id UUID NOT NULL,"
            + "event_type VARCHAR(100) NOT NULL, payload_hash VARCHAR(64) NOT NULL, status VARCHAR(24) NOT NULL,"
            + "attempts INTEGER NOT NULL, received_at TIMESTAMP WITH TIME ZONE NOT NULL,"
            + "claimed_at TIMESTAMP WITH TIME ZONE, processed_at TIMESTAMP WITH TIME ZONE, last_error TEXT,"
            + "CONSTRAINT ux_inbox UNIQUE(consumer_name,event_id))");
    outbox = new DurableEventOutbox(jdbc, new ObjectMapper());
    operations = new ReliableOutboxOperations(jdbc);
  }

  @Test
  void persistsDeadLettersAndAllowsExplicitReplay() {
    UUID id = outbox.append("test.events.v1", "aggregate-1", Map.of("schemaVersion", 1));

    @SuppressWarnings("unchecked")
    KafkaTemplate<String, byte[]> kafka = mock(KafkaTemplate.class);
    when(kafka.send(anyString(), anyString(), any(byte[].class)))
        .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("broker unavailable")));

    ReliableOutboxPublisher publisher =
        new ReliableOutboxPublisher(jdbc, kafka, operations, 10, 1, 1, Duration.ofSeconds(30), 30);
    publisher.publishPending();

    OutboxRecord failed = operations.list("DEAD_LETTER", 0, 10).getFirst();
    assertEquals(id, failed.id());
    assertEquals(1, failed.attempts());
    assertNotNull(failed.deadLetteredAt());

    assertTrue(operations.replay(id));
    OutboxRecord replayed = operations.list("PENDING", 0, 10).getFirst();
    assertEquals(1, replayed.replayCount());
    assertEquals(0, replayed.attempts());
  }

  @Test
  void reconciliationRecoversExpiredPublisherClaims() {
    UUID id = outbox.appendRaw("test.events.v1", "aggregate-2", "{}");
    jdbc.update(
        "UPDATE reliable_event_outbox SET status='PROCESSING', claimed_at=DATEADD('MINUTE', -10, CURRENT_TIMESTAMP) WHERE id=?",
        id);

    assertEquals(1, operations.recoverExpiredClaims(Duration.ofMinutes(2)));
    assertEquals("FAILED", operations.list("FAILED", 0, 10).getFirst().status());
  }

  @Test
  void inboxSuppressesDuplicatesAndRetriesFailedHandlers() {
    ReliableEventInbox inbox =
        new ReliableEventInbox(jdbc, new DataSourceTransactionManager(jdbc.getDataSource()));
    UUID eventId = UUID.randomUUID();
    java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();

    assertTrue(inbox.processOnce(eventId, "consumer-a", "TEST", "{}".getBytes(), calls::incrementAndGet));
    assertFalse(inbox.processOnce(eventId, "consumer-a", "TEST", "{}".getBytes(), calls::incrementAndGet));
    assertEquals(1, calls.get());

    UUID failedId = UUID.randomUUID();
    assertThrows(
        IllegalStateException.class,
        () ->
            inbox.processOnce(
                failedId,
                "consumer-a",
                "POISON",
                "{}".getBytes(),
                () -> { throw new IllegalStateException("handler failed"); }));
    assertEquals("FAILED", inbox.list("FAILED", 0, 10).getFirst().status());
    assertTrue(inbox.processOnce(failedId, "consumer-a", "POISON", "{}".getBytes(), () -> {}));
  }

  @Test
  void concurrentInboxDeliveriesRunTheHandlerOnce() throws Exception {
    ReliableEventInbox inbox =
        new ReliableEventInbox(jdbc, new DataSourceTransactionManager(jdbc.getDataSource()));
    UUID eventId = UUID.randomUUID();
    java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
    CountDownLatch start = new CountDownLatch(1);
    try (var executor = Executors.newFixedThreadPool(2)) {
      java.util.concurrent.Callable<Boolean> delivery =
          () -> {
            start.await(5, TimeUnit.SECONDS);
            return inbox.processOnce(
                eventId, "concurrent-consumer", "TEST", "{}".getBytes(), calls::incrementAndGet);
          };
      Future<Boolean> first = executor.submit(delivery);
      Future<Boolean> second = executor.submit(delivery);
      start.countDown();
      assertEquals(1, java.util.stream.Stream.of(first.get(), second.get()).filter(Boolean::booleanValue).count());
    }
    assertEquals(1, calls.get());
  }
}
