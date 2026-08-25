package git.jogindermikael.patientportalservice.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class PatientPortalModels {
    private PatientPortalModels() {
    }

    public record PortalOverview(UUID patientId, String accountStatus, List<PortalAppointmentRequest> appointments, List<RecordAccessRequest> recordRequests, List<PortalPayment> payments) {}
    public record PortalAppointmentRequest(UUID id, UUID patientId, String preferredSpecialty, String reason, String status, Instant createdAt) {}
    public record RecordAccessRequest(UUID id, UUID patientId, String recordType, String status, Instant createdAt) {}
    public record PortalPayment(UUID id, UUID patientId, UUID invoiceId, BigDecimal amount, String status, Instant createdAt) {}
}
