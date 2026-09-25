package git.jogindermikael.patientportalservice.model;

import java.util.List;
import java.util.UUID;

public record PortalOverview(UUID patientId, String accountStatus,
        List<PortalAppointmentRequest> appointments, List<RecordAccessRequest> recordRequests,
        List<PortalPayment> payments) {
}
