package git.jogindermikael.billingservice.repository;

import git.jogindermikael.billingservice.model.BillingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface BillingAccountRepository extends JpaRepository<BillingAccount, UUID> {
    Optional<BillingAccount> findByPatientId(UUID patientId);
    Optional<BillingAccount> findByIdempotencyKey(String idempotencyKey);
}
