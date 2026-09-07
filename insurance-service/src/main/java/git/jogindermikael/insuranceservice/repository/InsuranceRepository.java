package git.jogindermikael.insuranceservice.repository;

import git.jogindermikael.insuranceservice.model.InsuranceModels.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InsuranceRepository {
  private final JdbcTemplate jdbc;

  public InsuranceRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public void lock() {
    jdbc.queryForObject("SELECT id FROM workflow_lock WHERE id=1 FOR UPDATE", Integer.class);
  }

  private InsurancePolicy save(InsurancePolicy v) {
    if (jdbc.update(
            "UPDATE insurance_policy SET patient_id=?, provider_name=?, member_number=?,"
                + " plan_name=?, status=?, created_at=? WHERE id=?",
            v.patientId(),
            v.providerName(),
            v.memberNumber(),
            v.planName(),
            v.status(),
            v.createdAt().atOffset(ZoneOffset.UTC),
            v.id())
        == 0)
      jdbc.update(
          "INSERT INTO insurance_policy VALUES (?,?,?,?,?,?,?)",
          v.id(),
          v.patientId(),
          v.providerName(),
          v.memberNumber(),
          v.planName(),
          v.status(),
          v.createdAt().atOffset(ZoneOffset.UTC));
    return v;
  }

  private List<InsurancePolicy> policies(String where, Object... args) {
    return jdbc.query(
        "SELECT * FROM insurance_policy " + where + " ORDER BY id",
        (rs, row) ->
            new InsurancePolicy(
                rs.getObject("id", UUID.class),
                rs.getObject("patient_id", UUID.class),
                rs.getString("provider_name"),
                rs.getString("member_number"),
                rs.getString("plan_name"),
                rs.getString("status"),
                rs.getObject("created_at", java.time.OffsetDateTime.class).toInstant()),
        args);
  }

  private CoverageVerification save(CoverageVerification v) {
    if (jdbc.update(
            "UPDATE coverage_verification SET policy_id=?, service_code=?, status=?,"
                + " insurance_responsibility=?, patient_responsibility=?, verified_at=? WHERE id=?",
            v.policyId(),
            v.serviceCode(),
            v.status(),
            v.insuranceResponsibility(),
            v.patientResponsibility(),
            v.verifiedAt().atOffset(ZoneOffset.UTC),
            v.id())
        == 0)
      jdbc.update(
          "INSERT INTO coverage_verification VALUES (?,?,?,?,?,?,?)",
          v.id(),
          v.policyId(),
          v.serviceCode(),
          v.status(),
          v.insuranceResponsibility(),
          v.patientResponsibility(),
          v.verifiedAt().atOffset(ZoneOffset.UTC));
    return v;
  }

  private List<CoverageVerification> verifications(String where, Object... args) {
    return jdbc.query(
        "SELECT * FROM coverage_verification " + where + " ORDER BY id",
        (rs, row) ->
            new CoverageVerification(
                rs.getObject("id", UUID.class),
                rs.getObject("policy_id", UUID.class),
                rs.getString("service_code"),
                rs.getString("status"),
                rs.getBigDecimal("insurance_responsibility"),
                rs.getBigDecimal("patient_responsibility"),
                rs.getObject("verified_at", java.time.OffsetDateTime.class).toInstant()),
        args);
  }

  private Claim save(Claim v) {
    if (jdbc.update(
            "UPDATE claim SET patient_id=?, policy_id=?, invoice_id=?, amount=?, status=?,"
                + " updated_at=? WHERE id=?",
            v.patientId(),
            v.policyId(),
            v.invoiceId(),
            v.amount(),
            v.status(),
            v.updatedAt().atOffset(ZoneOffset.UTC),
            v.id())
        == 0)
      jdbc.update(
          "INSERT INTO claim VALUES (?,?,?,?,?,?,?)",
          v.id(),
          v.patientId(),
          v.policyId(),
          v.invoiceId(),
          v.amount(),
          v.status(),
          v.updatedAt().atOffset(ZoneOffset.UTC));
    return v;
  }

  private List<Claim> claims(String where, Object... args) {
    return jdbc.query(
        "SELECT * FROM claim " + where + " ORDER BY id",
        (rs, row) ->
            new Claim(
                rs.getObject("id", UUID.class),
                rs.getObject("patient_id", UUID.class),
                rs.getObject("policy_id", UUID.class),
                rs.getObject("invoice_id", UUID.class),
                rs.getBigDecimal("amount"),
                rs.getString("status"),
                rs.getObject("updated_at", java.time.OffsetDateTime.class).toInstant()),
        args);
  }

  public InsurancePolicy savePolicy(InsurancePolicy policy) {
    return save(policy);
  }

  public Collection<InsurancePolicy> findPolicies() {
    return policies("");
  }

  public CoverageVerification saveVerification(CoverageVerification verification) {
    return save(verification);
  }

  public Collection<CoverageVerification> findVerifications() {
    return verifications("");
  }

  public Claim saveClaim(Claim claim) {
    return save(claim);
  }

  public Optional<Claim> findClaimById(UUID id) {
    return claims("WHERE id=?", id).stream().findFirst();
  }

  public Collection<Claim> findClaims() {
    return claims("");
  }

  public Collection<InsurancePolicy> policiesForPatient(UUID id) {
    return policies("WHERE patient_id=?", id);
  }

  public Collection<Claim> claimsForPatient(UUID id) {
    return claims("WHERE patient_id=?", id);
  }
}
