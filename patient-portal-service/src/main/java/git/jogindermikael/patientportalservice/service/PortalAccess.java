package git.jogindermikael.patientportalservice.service;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PortalAccess {
  private final JdbcTemplate jdbc;

  public PortalAccess(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public String actor() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated())
      throw new AccessDeniedException("Identity required");
    return auth.getName();
  }

  public boolean admin() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    return auth != null
        && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
  }

  public boolean owner(UUID patient) {
    return jdbc.queryForObject(
            "SELECT COUNT(*) FROM portal_identity WHERE subject=? AND patient_id=?"
                + " AND status='ACTIVE' AND patient_status='ACTIVE'",
            Integer.class,
            actor(),
            patient)
        > 0;
  }

  public UUID patientForActor() {
    var matches = jdbc.queryForList(
        "SELECT patient_id FROM portal_identity WHERE subject=?"
            + " AND status='ACTIVE' AND patient_status='ACTIVE'",
        actor());
    if (matches.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "PORTAL_ENROLLMENT_REQUIRED");
    }
    return (UUID) matches.getFirst().get("patient_id");
  }

  public void requireOwner(UUID patient) {
    if (!admin() && !owner(patient)) throw new AccessDeniedException("Patient owner required");
  }

  public void require(UUID patient, String scope) {
    if (admin() || owner(patient)) return;
    if (jdbc.queryForObject(
            "SELECT COUNT(*) FROM proxy_grant WHERE patient_id=? AND proxy_subject=? AND scope=?"
                + " AND revoked=FALSE AND expires_at>CURRENT_TIMESTAMP",
            Integer.class,
            patient,
            actor(),
            scope)
        == 0) throw new AccessDeniedException("Patient access denied");
  }

  public void history(UUID id, String action) {
    jdbc.update(
        "INSERT INTO portal_history VALUES (?,?,?,?,CURRENT_TIMESTAMP)",
        UUID.randomUUID(),
        id,
        actor(),
        action);
  }
}
