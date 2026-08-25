package git.jogindermikael.insuranceservice.repository;

import git.jogindermikael.insuranceservice.model.InsuranceModels.*;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InsuranceRepository {
    private final ConcurrentHashMap<UUID, InsurancePolicy> policies = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, CoverageVerification> verifications = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Claim> claims = new ConcurrentHashMap<>();

    public InsurancePolicy savePolicy(InsurancePolicy policy) { policies.put(policy.id(), policy); return policy; }
    public Collection<InsurancePolicy> findPolicies() { return policies.values(); }
    public CoverageVerification saveVerification(CoverageVerification verification) { verifications.put(verification.id(), verification); return verification; }
    public Collection<CoverageVerification> findVerifications() { return verifications.values(); }
    public Claim saveClaim(Claim claim) { claims.put(claim.id(), claim); return claim; }
    public Optional<Claim> findClaimById(UUID id) { return Optional.ofNullable(claims.get(id)); }
    public Collection<Claim> findClaims() { return claims.values(); }
}
