package git.jogindermikael.auditcomplianceservice.service;

import git.jogindermikael.auditcomplianceservice.model.Kind;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ComplianceCaseService {
    private static final Map<String,Set<String>> TRANSITIONS = Map.of(
        "OPEN", Set.of("INVESTIGATING"),
        "INVESTIGATING", Set.of("CONTAINED", "REMEDIATING"),
        "CONTAINED", Set.of("ASSESSED"),
        "ASSESSED", Set.of("NOTIFICATION_REQUIRED", "NO_NOTIFICATION_REQUIRED"),
        "NOTIFICATION_REQUIRED", Set.of("NOTIFIED"),
        "NOTIFIED", Set.of("REMEDIATING"),
        "NO_NOTIFICATION_REQUIRED", Set.of("REMEDIATING"),
        "REMEDIATING", Set.of("CLOSED"));
    private final JdbcTemplate db;
    public ComplianceCaseService(JdbcTemplate db) { this.db = db; }

    @Transactional
    public UUID create(Kind kind, String owner, String evidence, Instant due, String actor) {
        if (!due.isAfter(Instant.now())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date must be future");
        UUID id = UUID.randomUUID();
        Timestamp now = Timestamp.from(Instant.now());
        db.update("INSERT INTO compliance_case(id,kind,status,owner,evidence,due_at,created_at,updated_at,legal_hold) VALUES (?,?,'OPEN',?,?,?,?,?,FALSE)",
                id, kind.name(), owner, evidence, Timestamp.from(due), now, now);
        history(id, actor, "OPEN", evidence);
        return id;
    }

    @Transactional
    public void transition(UUID id, String expectedStatus, String next, String evidence, String actor) {
        Map<String,Object> row = locked(id);
        String current = (String)row.get("status");
        boolean incident = "INCIDENT".equals(row.get("kind"));
        if (!current.equals(expectedStatus) || !TRANSITIONS.getOrDefault(current, Set.of()).contains(next)
                || (incident && current.equals("INVESTIGATING") && next.equals("REMEDIATING"))
                || (!incident && current.equals("INVESTIGATING") && !next.equals("REMEDIATING"))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid or stale workflow transition");
        }
        if (next.equals("CLOSED") && Boolean.TRUE.equals(row.get("legal_hold"))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Release legal hold before closing");
        }
        db.update("UPDATE compliance_case SET status=?,evidence=?,updated_at=? WHERE id=?", next, evidence, Timestamp.from(Instant.now()), id);
        history(id, actor, next, evidence);
    }

    @Transactional
    public void hold(UUID id, boolean hold, String evidence, String actor) {
        locked(id);
        db.update("UPDATE compliance_case SET legal_hold=?,updated_at=? WHERE id=?", hold, Timestamp.from(Instant.now()), id);
        history(id, actor, hold ? "HOLD_PLACED" : "HOLD_RELEASED", evidence);
    }

    @Transactional(readOnly=true)
    public List<Map<String,Object>> list(boolean overdue) {
        return overdue ? db.queryForList("SELECT * FROM compliance_case WHERE status<>'CLOSED' AND due_at<? ORDER BY due_at LIMIT 100", Timestamp.from(Instant.now()))
                : db.queryForList("SELECT * FROM compliance_case ORDER BY created_at DESC LIMIT 100");
    }

    @Transactional(readOnly=true)
    public List<Map<String,Object>> history(UUID id) {
        return db.queryForList("SELECT * FROM compliance_history WHERE case_id=? ORDER BY occurred_at,id", id);
    }
    private Map<String,Object> locked(UUID id) {
        return db.queryForList("SELECT * FROM compliance_case WHERE id=? FOR UPDATE", id).stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    private void history(UUID id, String actor, String action, String evidence) {
        db.update("INSERT INTO compliance_history VALUES (?,?,?,?,?,?)", UUID.randomUUID(), id, actor, action, evidence, Timestamp.from(Instant.now()));
    }
}
