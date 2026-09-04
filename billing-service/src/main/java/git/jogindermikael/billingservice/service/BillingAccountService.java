package git.jogindermikael.billingservice.service;

import git.jogindermikael.billingservice.model.BillingAccount;
import git.jogindermikael.billingservice.repository.BillingAccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;

@Service
public class BillingAccountService {
    private final BillingAccountRepository repository;
    public BillingAccountService(BillingAccountRepository repository) { this.repository = repository; }

    @Transactional
    public BillingAccount create(UUID patientId, String idempotencyKey) {
        if (patientId == null) throw new IllegalArgumentException("patientId is required");
        String effectiveKey = idempotencyKey == null || idempotencyKey.isBlank()
                ? "patient:" + patientId : idempotencyKey.trim();
        BillingAccount keyed = repository.findByIdempotencyKey(effectiveKey).orElse(null);
        if (keyed != null) {
            if (!keyed.getPatientId().equals(patientId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency key is already associated with another patient");
            }
            return keyed;
        }
        BillingAccount patientAccount = repository.findByPatientId(patientId).orElse(null);
        if (patientAccount != null) return patientAccount;
        try {
            return repository.saveAndFlush(new BillingAccount(patientId, effectiveKey));
        } catch (DataIntegrityViolationException race) {
            return repository.findByPatientId(patientId).orElseThrow(() -> race);
        }
    }

    @Transactional(readOnly = true)
    public BillingAccount get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Billing account not found"));
    }

    @Transactional(readOnly = true)
    public BillingAccount forPatient(UUID patientId) {
        return repository.findByPatientId(patientId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Billing account not found"));
    }

    @Transactional(readOnly = true)
    public List<BillingAccount> list() { return repository.findAll(); }
}
