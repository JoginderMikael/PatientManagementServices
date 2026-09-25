package git.jogindermikael.patientportalservice.mapper;

import git.jogindermikael.patientportalservice.dto.*;
import git.jogindermikael.patientportalservice.model.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PatientPortalMapper {
    public PortalAppointmentRequest toAppointmentRequest(PortalAppointmentCommand request) {
        return new PortalAppointmentRequest(UUID.randomUUID(), request.patientId(), request.preferredSpecialty(), request.reason(), "REQUESTED", Instant.now());
    }

    public RecordAccessRequest toRecordAccessRequest(RecordAccessCommand request) {
        return new RecordAccessRequest(UUID.randomUUID(), request.patientId(), request.recordType(), "READY_FOR_REVIEW", Instant.now());
    }

    public PortalPayment toPayment(PortalPaymentCommand request) {
        return new PortalPayment(UUID.randomUUID(), request.patientId(), request.invoiceId(), request.amount(), "SUBMITTED", Instant.now());
    }
}
