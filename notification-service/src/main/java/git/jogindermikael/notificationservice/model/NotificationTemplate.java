package git.jogindermikael.notificationservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "notification_template")
public class NotificationTemplate {
  @Id private UUID id;
  @Column(nullable = false) private String templateKey;
  @Column(nullable = false) private String locale;
  @Column(nullable = false) private String channel;
  private String subject;
  @Column(nullable = false, columnDefinition = "TEXT") private String body;
  @Column(nullable = false) private boolean active;
  @Column(nullable = false) private Instant createdAt;
  @Column(nullable = false) private Instant updatedAt;
  @Version private long version;

  protected NotificationTemplate() {}

  public NotificationTemplate(
      UUID id, String templateKey, String locale, String channel, String subject, String body) {
    this.id = id;
    this.templateKey = templateKey;
    this.locale = locale;
    this.channel = channel.toUpperCase();
    this.subject = subject;
    this.body = body;
    this.active = true;
    this.createdAt = Instant.now();
    this.updatedAt = createdAt;
  }

  public UUID getId() { return id; }
  public String getTemplateKey() { return templateKey; }
  public String getLocale() { return locale; }
  public String getChannel() { return channel; }
  public String getSubject() { return subject; }
  public String getBody() { return body; }
  public boolean isActive() { return active; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public long getVersion() { return version; }

  public String render(Map<String, String> variables) {
    String rendered = body;
    for (Map.Entry<String, String> entry : variables.entrySet()) {
      rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue());
    }
    if (rendered.matches("(?s).*\\{\\{[A-Za-z0-9_.-]+}}.*")) {
      throw new IllegalArgumentException("Template variables are incomplete");
    }
    return rendered;
  }
}
