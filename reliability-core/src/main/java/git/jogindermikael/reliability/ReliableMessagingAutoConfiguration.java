package git.jogindermikael.reliability;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration
@EnableScheduling
@ConditionalOnClass({JdbcTemplate.class, KafkaTemplate.class})
@ConditionalOnProperty(
    name = "app.reliability.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class ReliableMessagingAutoConfiguration {
  @Bean
  @ConditionalOnMissingBean
  DurableEventOutbox durableEventOutbox(JdbcTemplate jdbc, ObjectMapper mapper) {
    return new DurableEventOutbox(jdbc, mapper);
  }

  @Bean
  @ConditionalOnMissingBean
  ReliableOutboxOperations reliableOutboxOperations(JdbcTemplate jdbc) {
    return new ReliableOutboxOperations(jdbc);
  }

  @Bean
  @ConditionalOnMissingBean
  ReliableEventInbox reliableEventInbox(
      JdbcTemplate jdbc, PlatformTransactionManager transactionManager) {
    return new ReliableEventInbox(jdbc, transactionManager);
  }

  @Bean
  @ConditionalOnMissingBean
  ReliableOutboxPublisher reliableOutboxPublisher(
      JdbcTemplate jdbc,
      KafkaTemplate<String, byte[]> kafka,
      ReliableOutboxOperations operations,
      @Value("${app.reliability.batch-size:50}") int batchSize,
      @Value("${app.reliability.max-attempts:10}") int maxAttempts,
      @Value("${app.reliability.send-timeout-seconds:10}") long sendTimeoutSeconds,
      @Value("${app.reliability.claim-lease-seconds:120}") long claimLeaseSeconds,
      @Value("${app.reliability.retention-days:30}") int retentionDays) {
    return new ReliableOutboxPublisher(
        jdbc,
        kafka,
        operations,
        batchSize,
        maxAttempts,
        sendTimeoutSeconds,
        Duration.ofSeconds(claimLeaseSeconds),
        retentionDays);
  }

  @Bean
  @ConditionalOnMissingBean
  OutboxOperationsController outboxOperationsController(
      ReliableOutboxOperations operations,
      @Value("${app.reliability.claim-lease-seconds:120}") long claimLeaseSeconds) {
    return new OutboxOperationsController(operations, Duration.ofSeconds(claimLeaseSeconds));
  }

  @Bean
  @ConditionalOnMissingBean
  InboxOperationsController inboxOperationsController(
      ReliableEventInbox inbox,
      @Value("${app.reliability.inbox-lease-seconds:300}") long leaseSeconds) {
    return new InboxOperationsController(inbox, Duration.ofSeconds(leaseSeconds));
  }
}
