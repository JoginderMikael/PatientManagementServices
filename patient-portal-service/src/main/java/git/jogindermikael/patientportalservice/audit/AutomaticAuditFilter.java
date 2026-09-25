package git.jogindermikael.patientportalservice.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import git.jogindermikael.reliability.DurableEventOutbox;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@ConditionalOnProperty(name = "app.audit.enabled", havingValue = "true", matchIfMissing = true)
public class AutomaticAuditFilter extends OncePerRequestFilter {
  private final DurableEventOutbox outbox;
  private final ObjectMapper mapper;
  private final String source;

  public AutomaticAuditFilter(
      DurableEventOutbox outbox,
      ObjectMapper mapper,
      @Value("${spring.application.name}") String source) {
    this.outbox = outbox;
    this.mapper = mapper;
    this.source = source;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/actuator")
        || path.startsWith("/internal/reliability")
        || path.contains("api-docs")
        || path.startsWith("/swagger");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    try {
      chain.doFilter(request, response);
    } finally {
      capture(request, response);
    }
  }

  private void capture(HttpServletRequest request, HttpServletResponse response) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) return;
    try {
      UUID eventId = UUID.randomUUID();
      Map<String, Object> event = new LinkedHashMap<>();
      event.put("eventId", eventId);
      event.put("schemaVersion", 1);
      event.put("eventType", "HTTP_" + request.getMethod());
      event.put("occurredAt", Instant.now());
      event.put("source", source);
      event.put("aggregateType", "HTTP");
      event.put("aggregateId", uuidOrNull(lastPathSegment(request.getRequestURI())));
      event.put("patientId", uuidOrNull(request.getParameter("patientId")));
      event.put("actorId", authentication.getName());
      event.put(
          "actorRole",
          authentication.getAuthorities().stream()
              .findFirst()
              .map(Object::toString)
              .orElse("AUTHENTICATED"));
      event.put("correlationId", headerOrId(request, "X-Correlation-Id", eventId.toString()));
      event.put("requestId", headerOrId(request, "X-Request-Id", eventId.toString()));
      event.put("endpoint", request.getMethod() + " " + request.getRequestURI());
      event.put("outcome", response.getStatus() < 400 ? "SUCCESS" : "FAILURE");
      outbox.appendRaw("audit.events.v1", eventId.toString(), mapper.writeValueAsString(event));
    } catch (Exception exception) {
      logger.error("Automatic audit event could not be persisted", exception);
    }
  }

  private String headerOrId(HttpServletRequest request, String name, String fallback) {
    String value = request.getHeader(name);
    return value == null || value.isBlank() ? fallback : value;
  }

  private String lastPathSegment(String path) {
    String[] segments = path.split("/");
    return segments.length == 0 ? "" : segments[segments.length - 1];
  }

  private String uuidOrNull(String value) {
    if (value == null) return null;
    try {
      return UUID.fromString(value).toString();
    } catch (IllegalArgumentException ignored) {
      return null;
    }
  }
}
