package git.jogindermikael.insuranceservice.integration;

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
public class WorkflowClient {
  private final RestClient billing;
  private final RestClient staff;

  public WorkflowClient(
      @Value("${app.billing.url:http://billing-service:4001}") String billingUrl,
      @Value("${app.staff.url:http://staff-dashboard-service:4017}") String staffUrl) {
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(3000);
    factory.setReadTimeout(5000);
    billing = RestClient.builder().baseUrl(billingUrl).requestFactory(factory).build();
    staff = RestClient.builder().baseUrl(staffUrl).requestFactory(factory).build();
  }

  private String token() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (!(auth instanceof JwtAuthenticationToken jwt))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bearer identity required");
    return jwt.getToken().getTokenValue();
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> invoice(UUID id) {
    return billing
        .get()
        .uri("/billing/invoices/" + id)
        .headers(h -> h.setBearerAuth(token()))
        .retrieve()
        .body(Map.class);
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> createInvoice(Object body) {
    return billing
        .post()
        .uri("/billing/invoices")
        .headers(h -> h.setBearerAuth(token()))
        .body(body)
        .retrieve()
        .body(Map.class);
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> post(UUID invoice, Object body) {
    return billing
        .post()
        .uri("/billing/invoices/" + invoice + "/postings")
        .headers(h -> h.setBearerAuth(token()))
        .body(body)
        .retrieve()
        .body(Map.class);
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> task(Object body) {
    return staff
        .post()
        .uri("/staff-dashboard/clinical-tasks")
        .headers(h -> h.setBearerAuth(token()))
        .body(body)
        .retrieve()
        .body(Map.class);
  }
}
