package git.jogindermikael.notificationservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "notification_preference")
public class NotificationPreference {
  @Id private UUID recipientId;
  @Column(nullable = false) private String timezone;
  private LocalTime quietStart;
  private LocalTime quietEnd;
  @Column(nullable = false) private String locale;
  @Column(nullable = false) private boolean optOut;
  @Column(nullable = false) private String allowedChannels;
  @Column(nullable = false) private Instant updatedAt;
  @Version private long version;

  protected NotificationPreference() {}

  public NotificationPreference(
      UUID recipientId,
      String timezone,
      LocalTime quietStart,
      LocalTime quietEnd,
      String locale,
      boolean optOut,
      Set<String> allowedChannels) {
    this.recipientId = recipientId;
    this.timezone = ZoneId.of(timezone).getId();
    this.quietStart = quietStart;
    this.quietEnd = quietEnd;
    this.locale = locale;
    this.optOut = optOut;
    this.allowedChannels =
        allowedChannels.stream()
            .map(String::toUpperCase)
            .sorted()
            .collect(Collectors.joining(","));
    this.updatedAt = Instant.now();
  }

  public UUID getRecipientId() { return recipientId; }
  public String getTimezone() { return timezone; }
  public LocalTime getQuietStart() { return quietStart; }
  public LocalTime getQuietEnd() { return quietEnd; }
  public String getLocale() { return locale; }
  public boolean isOptOut() { return optOut; }
  public String getAllowedChannels() { return allowedChannels; }
  public Instant getUpdatedAt() { return updatedAt; }
  public long getVersion() { return version; }

  public Set<String> channelSet() {
    return Arrays.stream(allowedChannels.split(","))
        .filter(value -> !value.isBlank())
        .collect(Collectors.toUnmodifiableSet());
  }

  public Instant nextAllowedDelivery(Instant now) {
    if (quietStart == null || quietEnd == null || quietStart.equals(quietEnd)) return now;
    ZonedDateTime local = now.atZone(ZoneId.of(timezone));
    LocalTime time = local.toLocalTime();
    boolean crossesMidnight = quietStart.isAfter(quietEnd);
    boolean quiet =
        crossesMidnight
            ? !time.isBefore(quietStart) || time.isBefore(quietEnd)
            : !time.isBefore(quietStart) && time.isBefore(quietEnd);
    if (!quiet) return now;
    ZonedDateTime end = local.toLocalDate().atTime(quietEnd).atZone(local.getZone());
    if (crossesMidnight && !time.isBefore(quietStart)) end = end.plusDays(1);
    return end.toInstant();
  }
}
