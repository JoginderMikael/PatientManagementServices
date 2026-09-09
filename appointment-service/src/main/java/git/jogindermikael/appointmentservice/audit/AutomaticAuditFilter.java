package git.jogindermikael.appointmentservice.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.audit.enabled", havingValue = "true", matchIfMissing = true)
public class AutomaticAuditFilter extends OncePerRequestFilter {
    private final KafkaTemplate<String, byte[]> kafka;
    private final ObjectMapper mapper;
    private final String source;

    public AutomaticAuditFilter(KafkaTemplate<String, byte[]> kafka, ObjectMapper mapper,
            @Value("${spring.application.name}") String source) {
        this.kafka = kafka;
        this.mapper = mapper;
        this.source = source;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.contains("api-docs") || path.startsWith("/swagger");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } finally {
            capture(request, response);
        }
    }

    private void capture(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated())
            return;
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
            event.put("actorRole",
                    authentication.getAuthorities().stream().findFirst().map(Object::toString).orElse("AUTHENTICATED"));
            event.put("correlationId", headerOrId(request, "X-Correlation-Id", eventId.toString()));
            event.put("requestId", headerOrId(request, "X-Request-Id", eventId.toString()));
            event.put("endpoint", request.getMethod() + " " + request.getRequestURI());
            event.put("outcome", response.getStatus() < 400 ? "SUCCESS" : "FAILURE");
            kafka.send("audit.events.v1", eventId.toString(),
                    mapper.writeValueAsString(event).getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {
            logger.warn("Automatic audit event could not be published", ignored);
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
        if (value == null)
            return null;
        try {
            return UUID.fromString(value).toString();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
