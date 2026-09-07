package git.jogindermikael.inventorypharmacyservice.integration;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class PrescriptionSafetyClient {
  private final RestClient client;

  public PrescriptionSafetyClient(@Value("${app.ehr.url:http://ehr-service:4011}") String url) {
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(3000);
    factory.setReadTimeout(5000);
    client = RestClient.builder().baseUrl(url).requestFactory(factory).build();
  }

  public void verify(UUID prescription, UUID patient, String medication) {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (!(auth instanceof JwtAuthenticationToken jwt))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bearer identity required");
    var result =
        client
            .get()
            .uri("/ehr/safety/prescriptions/" + prescription + "/dispensing")
            .headers(h -> h.setBearerAuth(jwt.getToken().getTokenValue()))
            .retrieve()
            .body(Map.class);
    if (result == null
        || !patient.toString().equals(String.valueOf(result.get("patientId")))
        || !medication.strip().equalsIgnoreCase(String.valueOf(result.get("medication")))
        || !"ACTIVE".equals(result.get("status")))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "EHR prescription identity mismatch");
  }
}
