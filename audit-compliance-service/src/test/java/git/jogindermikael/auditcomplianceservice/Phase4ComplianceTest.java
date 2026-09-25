package git.jogindermikael.auditcomplianceservice;

import git.jogindermikael.auditcomplianceservice.model.Kind;
import git.jogindermikael.auditcomplianceservice.service.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties="spring.kafka.listener.auto-startup=false")
@AutoConfigureMockMvc
@Transactional
class Phase4ComplianceTest {
    @Autowired PrivacyService privacy;
    @Autowired ComplianceCaseService cases;
    @Autowired AuditComplianceService audit;
    @Autowired JdbcTemplate db;
    @Autowired MockMvc mvc;

    @Test void consentIsPatientAndSubjectBoundAndRevocationTakesEffect() {
        UUID patient = UUID.randomUUID();
        assertFalse(privacy.decide(patient, "doctor", null));
        UUID grant = privacy.grant(patient, "doctor", Instant.now().plusSeconds(3600), "evidence-1", "officer", false);
        assertTrue(privacy.decide(patient, "doctor", null));
        assertFalse(privacy.decide(patient, "other", null));
        assertFalse(privacy.decide(UUID.randomUUID(), "doctor", null));
        privacy.revoke(grant, "officer");
        assertFalse(privacy.decide(patient, "doctor", null));
    }

    @Test void emergencyRequiresExplicitIdExpiresAndRequiresIndependentReview() {
        UUID patient = UUID.randomUUID();
        UUID grant = privacy.grant(patient, "doctor", Instant.now().plusSeconds(1800), "emergency-ticket", "doctor", true);
        assertFalse(privacy.decide(patient, "doctor", null));
        assertTrue(privacy.decide(patient, "doctor", grant));
        assertFalse(privacy.decide(patient, "other", grant));
        assertThrows(ResponseStatusException.class, () -> privacy.review(grant, "doctor", "review"));
        db.update("UPDATE privacy_grant SET expires_at=? WHERE id=?", Timestamp.from(Instant.now().minusSeconds(1)), grant);
        assertFalse(privacy.decide(patient, "doctor", grant));
        assertEquals(1, privacy.pendingReviews().size());
        privacy.review(grant, "officer", "review-ticket");
        assertTrue(privacy.pendingReviews().isEmpty());
        assertFalse(privacy.decide(patient, "doctor", grant));
        assertThrows(ResponseStatusException.class, () -> privacy.grant(patient, "doctor", Instant.now().plusSeconds(3601), "ticket", "doctor", true));
    }

    @Test void incidentCannotSkipAssessmentNotificationOrLegalHold() {
        UUID id = cases.create(Kind.INCIDENT, "owner", "ticket", Instant.now().plusSeconds(3600), "officer");
        cases.transition(id, "OPEN", "INVESTIGATING", "triage", "officer");
        assertThrows(ResponseStatusException.class, () -> cases.transition(id, "INVESTIGATING", "REMEDIATING", "skip", "officer"));
        cases.transition(id, "INVESTIGATING", "CONTAINED", "contained", "officer");
        cases.transition(id, "CONTAINED", "ASSESSED", "assessment", "officer");
        cases.transition(id, "ASSESSED", "NOTIFICATION_REQUIRED", "decision", "officer");
        assertThrows(ResponseStatusException.class, () -> cases.transition(id, "NOTIFICATION_REQUIRED", "REMEDIATING", "skip", "officer"));
        cases.transition(id, "NOTIFICATION_REQUIRED", "NOTIFIED", "delivery-receipts", "officer");
        cases.transition(id, "NOTIFIED", "REMEDIATING", "fixes", "officer");
        cases.hold(id, true, "preservation", "officer");
        assertThrows(ResponseStatusException.class, () -> cases.transition(id, "REMEDIATING", "CLOSED", "closure", "officer"));
        cases.hold(id, false, "release", "officer");
        cases.transition(id, "REMEDIATING", "CLOSED", "closure", "officer");
        assertEquals(10, cases.history(id).size());
    }

    @Test void staleTransitionsAreRejected() {
        UUID id = cases.create(Kind.RESTORE_DRILL, "owner", "ticket", Instant.now().plusSeconds(3600), "officer");
        cases.transition(id, "OPEN", "INVESTIGATING", "start", "officer");
        assertThrows(ResponseStatusException.class, () -> cases.transition(id, "OPEN", "INVESTIGATING", "stale", "officer"));
        cases.transition(id, "INVESTIGATING", "REMEDIATING", "restore-report", "officer");
        cases.transition(id, "REMEDIATING", "CLOSED", "acceptance", "officer");
    }

    @Test void lateAuditEventsStillExtendLastAppendedHash() {
        var first = audit.append(UUID.randomUUID(), "actor", "role", "READ", null, "TEST", null, "test", "SUCCESS", null, null, null, null, Instant.now());
        var late = audit.append(UUID.randomUUID(), "actor", "role", "READ", null, "TEST", null, "test", "SUCCESS", null, null, null, null, Instant.now().minusSeconds(100));
        var last = audit.append(UUID.randomUUID(), "actor", "role", "READ", null, "TEST", null, "test", "SUCCESS", null, null, null, null, Instant.now());
        assertEquals(first.getEventHash(), late.getPreviousHash());
        assertEquals(late.getEventHash(), last.getPreviousHash());
    }

    @Test void privacyOfficerRoleIsRequiredAndDecisionUsesTokenSubject() throws Exception {
        mvc.perform(get("/compliance/cases")).andExpect(status().isUnauthorized());
        mvc.perform(get("/compliance/cases").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))).andExpect(status().isForbidden());
        mvc.perform(get("/compliance/cases").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PRIVACY_OFFICER")))).andExpect(status().isOk());
        UUID patient = UUID.randomUUID();
        privacy.grant(patient, "doctor", Instant.now().plusSeconds(300), "ticket", "officer", false);
        mvc.perform(post("/compliance/privacy/decisions/" + patient).param("subject", "doctor")
                .with(jwt().jwt(token -> token.subject("attacker")).authorities(new SimpleGrantedAuthority("ROLE_CLINICIAN"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.allowed").value(false));
    }
}
