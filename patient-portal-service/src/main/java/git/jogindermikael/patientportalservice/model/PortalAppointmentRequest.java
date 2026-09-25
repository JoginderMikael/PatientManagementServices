package git.jogindermikael.patientportalservice.model;

import java.time.Instant;
import java.util.UUID;

public record PortalAppointmentRequest(UUID id, UUID patientId, String preferredSpecialty,
        String reason, String status, Instant createdAt) {
}
