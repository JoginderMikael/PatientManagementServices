package git.jogindermikael.staffdashboardservice.service;

import java.util.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class StaffAccess {
  public static final Set<String> ROLES =
      Set.of(
          "ADMIN",
          "CLINICIAN",
          "NURSE",
          "RECEPTIONIST",
          "BILLING_STAFF",
          "PHARMACIST",
          "LAB_STAFF");

  public String actor() {
    var a = SecurityContextHolder.getContext().getAuthentication();
    if (a == null) throw new AccessDeniedException("Staff identity required");
    return a.getName();
  }

  public boolean has(String role) {
    var a = SecurityContextHolder.getContext().getAuthentication();
    return a != null
        && a.getAuthorities().stream().anyMatch(v -> v.getAuthority().equals("ROLE_" + role));
  }

  public void queue(String role) {
    if (!ROLES.contains(role) || (!has("ADMIN") && !has(role)))
      throw new AccessDeniedException("Queue role denied");
  }

  public void assignee(UUID id) {
    if (!has("ADMIN") && !actor().equals(id.toString()))
      throw new AccessDeniedException("Assigned staff required");
  }

  public String role() {
    return ROLES.stream()
        .filter(this::has)
        .sorted()
        .findFirst()
        .orElseThrow(() -> new AccessDeniedException("Staff role required"));
  }
}
