package git.jogindermikael.insuranceservice.model;

import java.time.Instant;
import java.util.UUID;

public record InsurancePolicy(UUID id, UUID patientId, String providerName, String memberNumber,
        String planName, String status, Instant createdAt) {
}
