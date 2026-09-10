package git.jogindermikael.patientportalservice.service;

import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class IdentityDirectoryClient {
  private final RestClient auth;
  private final RestClient patients;
  private final boolean validationEnabled;

  public IdentityDirectoryClient(
      @Value("${app.auth.url:http://auth-service:4005}") String authUrl,
      @Value("${app.patient.url:http://patient-service:4000}") String patientUrl,
      @Value("${app.identity.validation-enabled:true}") boolean validationEnabled) {
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(3000);
    factory.setReadTimeout(5000);
    this.auth = RestClient.builder().baseUrl(authUrl).requestFactory(factory).build();
    this.patients = RestClient.builder().baseUrl(patientUrl).requestFactory(factory).build();
    this.validationEnabled = validationEnabled;
  }

  public void validatePatientIdentity(String subject, UUID patientId) {
    if (!validationEnabled) return;
    final UUID authUserId;
    try {
      authUserId = UUID.fromString(subject);
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Auth subject must be a UUID");
    }

    try {
      Map<?, ?> user = auth.get().uri("/admin/users/{id}", authUserId)
          .headers(headers -> headers.setBearerAuth(token()))
          .retrieve().body(Map.class);
      if (user == null || !"PATIENT".equals(user.get("role")) || !"ACTIVE".equals(user.get("status"))) {
        throw new ResponseStatusException(
            HttpStatus.CONFLICT, "Auth user must be an active PATIENT");
      }

      Map<?, ?> patient = patients.get().uri("/patients/{id}", patientId)
          .headers(headers -> headers.setBearerAuth(token()))
          .retrieve().body(Map.class);
      if (patient == null || !"ACTIVE".equals(patient.get("status"))) {
        throw new ResponseStatusException(
            HttpStatus.CONFLICT, "Patient must exist and be active");
      }
    } catch (ResponseStatusException exception) {
      throw exception;
    } catch (RestClientException exception) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Unable to validate auth user and patient");
    }
  }

  private String token() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof JwtAuthenticationToken jwt)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bearer administrator required");
    }
    return jwt.getToken().getTokenValue();
  }
}
