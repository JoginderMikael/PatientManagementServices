package git.jogindermikael.insuranceservice.service;

import git.jogindermikael.insuranceservice.dto.InsuranceDtos.*;
import git.jogindermikael.insuranceservice.mapper.InsuranceMapper;
import git.jogindermikael.insuranceservice.model.InsuranceModels.*;
import git.jogindermikael.insuranceservice.repository.InsuranceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class InsuranceService {
    private final InsuranceRepository repository;
    private final InsuranceMapper mapper;

    public InsuranceService(InsuranceRepository repository, InsuranceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public InsurancePolicy registerPolicy(PolicyRequest request) { return repository.savePolicy(mapper.toPolicy(request)); }
    public List<InsurancePolicy> policiesForPatient(UUID patientId) { return repository.findPolicies().stream().filter(policy -> policy.patientId().equals(patientId)).toList(); }
    public CoverageVerification verifyCoverage(CoverageVerificationRequest request) { return repository.saveVerification(mapper.toVerification(request)); }
    public List<CoverageVerification> listCoverageVerifications() { return repository.findVerifications().stream().sorted(Comparator.comparing(CoverageVerification::verifiedAt)).toList(); }
    public Claim submitClaim(ClaimRequest request) { return repository.saveClaim(mapper.toClaim(request)); }

    public Claim adjudicateClaim(UUID id, ClaimAdjudicationRequest request) {
        Claim claim = repository.findClaimById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim not found"));
        return repository.saveClaim(mapper.toAdjudicatedClaim(claim, request));
    }

    public List<Claim> listClaims() { return repository.findClaims().stream().sorted(Comparator.comparing(Claim::updatedAt)).toList(); }
}
