package git.jogindermikael.insuranceservice.service;

import git.jogindermikael.insuranceservice.dto.*;
import git.jogindermikael.insuranceservice.mapper.InsuranceMapper;
import git.jogindermikael.insuranceservice.model.*;
import git.jogindermikael.insuranceservice.repository.InsuranceRepository;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.transaction.annotation.Transactional
@Service
public class InsuranceService {
  private final InsuranceRepository repository;
  private final InsuranceMapper mapper;
  private final git.jogindermikael.insuranceservice.integration.WorkflowClient downstream;
  private final org.springframework.jdbc.core.JdbcTemplate jdbc;

  public InsuranceService(
      InsuranceRepository repository,
      InsuranceMapper mapper,
      org.springframework.jdbc.core.JdbcTemplate jdbc,
      git.jogindermikael.insuranceservice.integration.WorkflowClient downstream) {
    this.downstream = downstream;
    this.jdbc = jdbc;
    this.repository = repository;
    this.mapper = mapper;
  }

  public InsurancePolicy registerPolicy(PolicyRequest request) {
    repository.lock();
    return repository.savePolicy(mapper.toPolicy(request));
  }

  public List<InsurancePolicy> policiesForPatient(UUID patientId) {
    return java.util.List.copyOf(repository.policiesForPatient(patientId));
  }

  public CoverageVerification verifyCoverage(CoverageVerificationRequest request) {
    repository.lock();
    policy(request.policyId());
    var evidence =
        jdbc.queryForList(
            "SELECT * FROM eligibility_evidence WHERE policy_id=? AND service_code=? AND"
                + " valid_until>=CURRENT_DATE",
            request.policyId(),
            request.serviceCode());
    if (evidence.isEmpty()) return repository.saveVerification(mapper.toVerification(request));
    var covered =
        request
            .estimatedCharge()
            .multiply((java.math.BigDecimal) evidence.getFirst().get("insurance_percent"))
            .divide(new java.math.BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
    return repository.saveVerification(
        new CoverageVerification(
            UUID.randomUUID(),
            request.policyId(),
            request.serviceCode(),
            "VERIFIED",
            covered,
            request.estimatedCharge().subtract(covered),
            java.time.Instant.now()));
  }

  public List<CoverageVerification> listCoverageVerifications() {
    return repository.findVerifications().stream()
        .sorted(Comparator.comparing(CoverageVerification::verifiedAt))
        .toList();
  }

  public Claim submitClaim(ClaimRequest request) {
    repository.lock();
    var policy = policy(request.policyId());
    if (!policy.patientId().equals(request.patientId()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Policy patient mismatch");
    var existing =
        repository.findClaims().stream()
            .filter(c -> c.invoiceId().equals(request.invoiceId()))
            .findFirst();
    if (existing.isPresent()) {
      var c = existing.get();
      if (!c.patientId().equals(request.patientId())
          || !c.policyId().equals(request.policyId())
          || c.amount().compareTo(request.amount()) != 0)
        throw new ResponseStatusException(
            HttpStatus.CONFLICT, "Invoice already claimed with different details");
      return c;
    }
    var invoice = downstream.invoice(request.invoiceId());
    if (!request.patientId().toString().equals(String.valueOf(invoice.get("patient_id")))
        || request.amount().compareTo(new java.math.BigDecimal(invoice.get("amount").toString()))
            > 0)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Claim must match the invoice patient and not exceed invoice amount");
    return repository.saveClaim(mapper.toClaim(request));
  }

  public Claim adjudicateClaim(UUID id, ClaimAdjudicationRequest request) {
    repository.lock();
    Claim claim =
        repository
            .findClaimById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim not found"));
    if (!java.util.Set.of("APPROVED", "DENIED").contains(request.status())
        || request.approvedAmount().compareTo(claim.amount()) > 0
        || ("DENIED".equals(request.status()) && request.approvedAmount().signum() != 0))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid adjudication");
    if (!"SUBMITTED".equals(claim.status()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Claim already adjudicated");
    jdbc.update("INSERT INTO claim_adjudication VALUES (?,?)", id, request.approvedAmount());
    return repository.saveClaim(mapper.toAdjudicatedClaim(claim, request));
  }

  public List<Claim> listClaims() {
    return repository.findClaims().stream().sorted(Comparator.comparing(Claim::updatedAt)).toList();
  }

  private InsurancePolicy policy(UUID id) {
    return repository.findPolicies().stream()
        .filter(p -> p.id().equals(id) && "ACTIVE".equals(p.status()))
        .findFirst()
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active policy not found"));
  }

  public void recordEvidence(UUID policyId, EligibilityEvidence evidence) {
    repository.lock();
    policy(policyId);
    jdbc.update("DELETE FROM eligibility_evidence WHERE policy_id=?", policyId);
    jdbc.update(
        "INSERT INTO eligibility_evidence VALUES (?,?,?,?,?)",
        policyId,
        evidence.serviceCode(),
        evidence.validUntil(),
        evidence.payerReference(),
        evidence.insurancePercent());
  }

  public java.util.Map<String, Object> remit(UUID id, RemittanceRequest request) {
    repository.lock();
    var old = jdbc.queryForList("SELECT * FROM remittance WHERE reference=?", request.reference());
    if (!old.isEmpty()) {
      var row = old.getFirst();
      if (!id.equals(row.get("claim_id"))
          || request.paidAmount().compareTo((java.math.BigDecimal) row.get("paid_amount")) != 0)
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Remittance reference reused");
      return row;
    }
    Claim claim =
        repository
            .findClaimById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim not found"));
    if (!"APPROVED".equals(claim.status()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Claim must be approved");
    var approved =
        jdbc.queryForObject(
            "SELECT approved_amount FROM claim_adjudication WHERE claim_id=?",
            java.math.BigDecimal.class,
            id);
    if (request.paidAmount().compareTo(approved) != 0)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Remittance must match approved amount");
    jdbc.update(
        "INSERT INTO remittance VALUES (?,?,?,?,CURRENT_TIMESTAMP)",
        UUID.randomUUID(),
        id,
        request.reference(),
        request.paidAmount());
    repository.saveClaim(
        new Claim(
            claim.id(),
            claim.patientId(),
            claim.policyId(),
            claim.invoiceId(),
            claim.amount(),
            "PAID",
            java.time.Instant.now()));
    return jdbc.queryForMap("SELECT * FROM remittance WHERE claim_id=?", id);
  }

  public java.util.Map<String, Object> reconcile(UUID id) {
    repository.lock();
    var claim =
        repository
            .findClaimById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim not found"));
    var remittances = jdbc.queryForList("SELECT * FROM remittance WHERE claim_id=?", id);
    if (remittances.isEmpty())
      throw new ResponseStatusException(HttpStatus.CONFLICT, "No remittance to reconcile");
    var invoice = downstream.invoice(claim.invoiceId());
    if (!claim.patientId().toString().equals(invoice.get("patient_id").toString()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Invoice patient mismatch");
    var row = remittances.getFirst();
    return downstream.post(
        claim.invoiceId(),
        java.util.Map.of(
            "reference",
            "remittance:" + row.get("reference"),
            "amount",
            row.get("paid_amount"),
            "kind",
            "REMITTANCE"));
  }
}
