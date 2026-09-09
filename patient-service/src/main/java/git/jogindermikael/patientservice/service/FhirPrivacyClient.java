package git.jogindermikael.patientservice.service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class FhirPrivacyClient {
    private final RestClient client;
    public FhirPrivacyClient(@Value("${app.privacy.base-url:http://audit-compliance-service:4015}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }
    public void requireAccess(UUID patient, UUID emergency, JwtAuthenticationToken token) {
        Map<?,?> decision;
        try {
            decision = client.post().uri(builder -> {
                builder.path("/compliance/privacy/decisions/{id}");
                if (emergency != null) builder.queryParam("emergencyId", emergency);
                return builder.build(patient);
            }).headers(headers -> headers.setBearerAuth(token.getToken().getTokenValue()))
                    .retrieve().body(Map.class);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Privacy decision unavailable");
        }
        if (decision == null || !(decision.get("allowed") instanceof Boolean)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Invalid privacy decision");
        }
        if (!Boolean.TRUE.equals(decision.get("allowed"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Patient access is not authorized");
        }
    }
}
