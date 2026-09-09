package git.jogindermikael.auditcomplianceservice.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PrivacyService {
    public static final String SCOPE = "FHIR_PATIENT_READ";
    private final JdbcTemplate db;
    private final AuditComplianceService audit;

    public PrivacyService(JdbcTemplate db, AuditComplianceService audit) {
        this.db = db;
        this.audit = audit;
    }

    private void lock() { db.queryForObject("SELECT id FROM privacy_lock WHERE id=1 FOR UPDATE", Integer.class); }

    @Transactional
    public UUID grant(UUID patient, String subject, Instant expiry, String evidence, String actor, boolean emergency) {
        lock();
        Instant now = Instant.now();
        if (!expiry.isAfter(now) || expiry.isAfter(now.plus(emergency ? 1 : 24 * 365, ChronoUnit.HOURS))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiry must be future and within the permitted duration");
        }
        UUID id = UUID.randomUUID();
        db.update("INSERT INTO privacy_grant(id,patient_id,subject,scope,kind,expires_at,revoked,created_by,created_at,evidence) VALUES (?,?,?,?,?,?,FALSE,?,?,?)",
                id, patient, subject, SCOPE, emergency ? "BREAK_GLASS" : "CONSENT", Timestamp.from(expiry), actor, Timestamp.from(now), evidence);
        event(actor, emergency ? "BREAK_GLASS_OPENED" : "CONSENT_GRANTED", patient, id, "SUCCESS");
        return id;
    }

    @Transactional
    public void revoke(UUID id, String actor) {
        lock();
        Map<String,Object> grant = find(id);
        db.update("UPDATE privacy_grant SET revoked=TRUE WHERE id=?", id);
        event(actor, "GRANT_REVOKED", (UUID)grant.get("patient_id"), id, "SUCCESS");
    }

    @Transactional
    public void review(UUID id, String actor, String evidence) {
        lock();
        Map<String,Object> grant = find(id);
        if (!"BREAK_GLASS".equals(grant.get("kind")) || actor.equals(grant.get("created_by")) || grant.get("reviewed_by") != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Requires an unreviewed emergency grant and an independent reviewer");
        }
        db.update("UPDATE privacy_grant SET reviewed_by=?,review_evidence=?,revoked=TRUE WHERE id=?", actor, evidence, id);
        event(actor, "BREAK_GLASS_REVIEWED", (UUID)grant.get("patient_id"), id, "SUCCESS");
    }

    @Transactional(readOnly=true)
    public List<Map<String,Object>> pendingReviews() {
        return db.queryForList("SELECT * FROM privacy_grant WHERE kind='BREAK_GLASS' AND reviewed_by IS NULL ORDER BY created_at LIMIT 100");
    }

    // The caller's verified JWT subject is used, never a subject supplied by the requesting client.
    @Transactional
    public boolean decide(UUID patient, String actor, UUID emergencyId) {
        lock();
        Instant now = Instant.now();
        Integer count = emergencyId == null
                ? db.queryForObject("SELECT COUNT(*) FROM privacy_grant WHERE patient_id=? AND subject=? AND scope=? AND kind='CONSENT' AND revoked=FALSE AND expires_at>?",
                    Integer.class, patient, actor, SCOPE, Timestamp.from(now))
                : db.queryForObject("SELECT COUNT(*) FROM privacy_grant WHERE id=? AND patient_id=? AND subject=? AND scope=? AND kind='BREAK_GLASS' AND revoked=FALSE AND expires_at>?",
                    Integer.class, emergencyId, patient, actor, SCOPE, Timestamp.from(now));
        boolean allowed = count != null && count > 0;
        event(actor, emergencyId == null ? "PRIVACY_DECISION" : "BREAK_GLASS_USE", patient, emergencyId, allowed ? "PERMIT" : "DENY");
        return allowed;
    }

    private Map<String,Object> find(UUID id) {
        return db.queryForList("SELECT * FROM privacy_grant WHERE id=?", id).stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void event(String actor, String action, UUID patient, UUID resource, String outcome) {
        audit.append(UUID.randomUUID(), actor, "", action, patient, "PRIVACY", resource,
                "audit-compliance-service", outcome, null, null, null, null, Instant.now());
    }
}
