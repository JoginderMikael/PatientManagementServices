package git.jogindermikael.patientportalservice.repository;

import git.jogindermikael.patientportalservice.model.PatientPortalModels.*;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PatientPortalRepository {
    private final ConcurrentHashMap<UUID, PortalAppointmentRequest> appointmentRequests = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, RecordAccessRequest> recordRequests = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, PortalPayment> payments = new ConcurrentHashMap<>();

    public PortalAppointmentRequest saveAppointmentRequest(PortalAppointmentRequest request) { appointmentRequests.put(request.id(), request); return request; }
    public Collection<PortalAppointmentRequest> findAppointmentRequests() { return appointmentRequests.values(); }
    public RecordAccessRequest saveRecordRequest(RecordAccessRequest request) { recordRequests.put(request.id(), request); return request; }
    public Collection<RecordAccessRequest> findRecordRequests() { return recordRequests.values(); }
    public PortalPayment savePayment(PortalPayment payment) { payments.put(payment.id(), payment); return payment; }
    public Collection<PortalPayment> findPayments() { return payments.values(); }
}
