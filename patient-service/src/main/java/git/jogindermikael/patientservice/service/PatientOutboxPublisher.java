package git.jogindermikael.patientservice.service;

import git.jogindermikael.patientservice.model.OutboxEvent;
import git.jogindermikael.patientservice.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;

@Component
public class PatientOutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(PatientOutboxPublisher.class);
    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    public PatientOutboxPublisher(OutboxEventRepository repository, KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.repository = repository; this.kafkaTemplate = kafkaTemplate;
    }
    @Scheduled(fixedDelayString = "${app.outbox.publish-delay-ms:1000}")
    @Transactional
    public void publishPending() {
        for (OutboxEvent event : repository.findTop100ByPublishedAtIsNullAndAttemptsLessThanOrderByCreatedAt(10)) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getEventKey(), event.getPayload().getBytes(StandardCharsets.UTF_8)).get();
                event.markPublished();
            } catch (Exception exception) {
                event.markAttempted();
                log.warn("Outbox event {} delivery attempt {} failed", event.getId(), event.getAttempts());
            }
        }
    }
}
