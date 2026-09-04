package git.jogindermikael.patientservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
public class OutboxEvent {
    @Id private UUID id;
    @Column(nullable = false, length = 100) private String topic;
    @Column(nullable = false, length = 100) private String eventKey;
    @Column(nullable = false, columnDefinition = "TEXT") private String payload;
    @Column(nullable = false) private Instant createdAt;
    private Instant publishedAt;
    @Column(nullable = false) private int attempts;

    protected OutboxEvent() {}
    public OutboxEvent(UUID id, String topic, String eventKey, String payload) {
        this.id = id; this.topic = topic; this.eventKey = eventKey; this.payload = payload; createdAt = Instant.now();
    }
    public UUID getId() { return id; }
    public String getTopic() { return topic; }
    public String getEventKey() { return eventKey; }
    public String getPayload() { return payload; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public int getAttempts() { return attempts; }
    public void markPublished() { publishedAt = Instant.now(); attempts++; }
    public void markAttempted() { attempts++; }
}
