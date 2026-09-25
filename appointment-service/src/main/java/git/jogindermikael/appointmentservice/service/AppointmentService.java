package git.jogindermikael.appointmentservice.service;

import git.jogindermikael.appointmentservice.dto.AppointmentDtos.*;
import git.jogindermikael.appointmentservice.mapper.AppointmentMapper;
import git.jogindermikael.appointmentservice.model.AppointmentModels.*;
import git.jogindermikael.appointmentservice.repository.AppointmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import git.jogindermikael.appointmentservice.repository.AppointmentSlotRepository;
import git.jogindermikael.appointmentservice.repository.AppointmentResourceRepository;
import git.jogindermikael.appointmentservice.repository.AppointmentResourceSlotRepository;
import git.jogindermikael.appointmentservice.repository.AppointmentJpaRepository;
import git.jogindermikael.appointmentservice.repository.WaitlistRepository;
import git.jogindermikael.reliability.DurableEventOutbox;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Service
public class AppointmentService {
    private final AppointmentRepository repository;
    private final AppointmentMapper mapper;
    private final AppointmentSlotRepository slotRepository;
    private final AppointmentResourceRepository resourceRepository;
    private final AppointmentResourceSlotRepository resourceSlotRepository;
    private final AppointmentJpaRepository appointmentJpaRepository;
    private final WaitlistRepository waitlistRepository;
    private final DurableEventOutbox outbox;

    public AppointmentService(AppointmentRepository repository, AppointmentMapper mapper,
            AppointmentSlotRepository slotRepository, AppointmentResourceRepository resourceRepository,
            AppointmentResourceSlotRepository resourceSlotRepository,
            AppointmentJpaRepository appointmentJpaRepository, WaitlistRepository waitlistRepository,
            DurableEventOutbox outbox) {
        this.repository = repository;
        this.mapper = mapper;
        this.slotRepository = slotRepository;
        this.resourceRepository = resourceRepository;
        this.resourceSlotRepository = resourceSlotRepository;
        this.appointmentJpaRepository = appointmentJpaRepository;
        this.waitlistRepository = waitlistRepository;
        this.outbox = outbox;
    }

    public List<DoctorSchedule> listSchedules() {
        return repository.findSchedules().stream().sorted(Comparator.comparing(DoctorSchedule::workDate)).toList();
    }

    public DoctorSchedule upsertSchedule(DoctorScheduleRequest request) {
        return repository.saveSchedule(mapper.toSchedule(request));
    }

    public List<Appointment> listAppointments(UUID patientId, UUID doctorId) {
        return repository.findAppointments().stream()
                .filter(appointment -> patientId == null || appointment.patientId().equals(patientId))
                .filter(appointment -> doctorId == null || appointment.doctorId().equals(doctorId))
                .sorted(Comparator.comparing(Appointment::startsAt))
                .toList();
    }

    public Page<Appointment> pageAppointments(int page, int size) {
        return appointmentJpaRepository.findAll(PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 200))));
    }

    @Transactional
    public Appointment bookAppointment(AppointmentRequest request) {
        Appointment appointment = repository.saveAppointment(mapper.toAppointment(request));
        allocateSlots(appointment, Set.of());
        emit("APPOINTMENT_BOOKED", appointment);
        return appointment;
    }

    private void allocateSlots(Appointment appointment, Set<UUID> resourceIds) {
        List<AppointmentSlot> slots = new ArrayList<>();
        LocalDateTime cursor = appointment.startsAt().truncatedTo(ChronoUnit.MINUTES);
        while (cursor.isBefore(appointment.endsAt())) {
            slots.add(new AppointmentSlot(appointment.id(), appointment.doctorId(), cursor));
            cursor = cursor.plusMinutes(1);
        }
        try {
            slotRepository.saveAllAndFlush(slots);
            if (resourceIds != null && !resourceIds.isEmpty()) {
                Set<UUID> active = resourceRepository.findAllById(resourceIds).stream()
                        .filter(AppointmentResource::isActive).map(AppointmentResource::getId)
                        .collect(java.util.stream.Collectors.toSet());
                if (active.size() != resourceIds.size()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more appointment resources are unavailable");
                }
                List<AppointmentResourceSlot> resourceSlots = new ArrayList<>();
                for (UUID resourceId : active) {
                    cursor = appointment.startsAt().truncatedTo(ChronoUnit.MINUTES);
                    while (cursor.isBefore(appointment.endsAt())) {
                        resourceSlots.add(new AppointmentResourceSlot(appointment.id(), resourceId, cursor));
                        cursor = cursor.plusMinutes(1);
                    }
                }
                resourceSlotRepository.saveAllAndFlush(resourceSlots);
            }
        } catch (DataIntegrityViolationException conflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The provider, room, or equipment is already booked during this interval", conflict);
        }
    }

    @Transactional
    public Appointment cancelAppointment(UUID id, CancellationRequest request) {
        Appointment appointment = getAppointment(id);
        if ("CANCELLED".equals(appointment.status())) return appointment;
        slotRepository.deleteByAppointmentId(id);
        resourceSlotRepository.deleteByAppointmentId(id);
        Appointment cancelled = repository.saveAppointment(mapper.toCancelledAppointment(appointment, request));
        emit("APPOINTMENT_CANCELLED", cancelled);
        return cancelled;
    }

    @Transactional
    public Appointment reschedule(UUID id, RescheduleRequest request) {
        Appointment appointment = getAppointment(id);
        slotRepository.deleteByAppointmentId(id);
        resourceSlotRepository.deleteByAppointmentId(id);
        try {
            appointment.reschedule(request.startsAt(), request.durationMinutes() == null ? 30 : request.durationMinutes());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
        allocateSlots(appointment, request.resourceIds() == null ? Set.of() : request.resourceIds());
        emit("APPOINTMENT_RESCHEDULED", appointment);
        return appointment;
    }

    @Transactional
    public Appointment checkIn(UUID id) { return transition(id, "APPOINTMENT_CHECKED_IN", Appointment::checkIn); }

    @Transactional
    public Appointment checkOut(UUID id) { return transition(id, "APPOINTMENT_COMPLETED", Appointment::checkOut); }

    @Transactional
    public Appointment markNoShow(UUID id) { return transition(id, "APPOINTMENT_NO_SHOW", Appointment::markNoShow); }

    private Appointment transition(UUID id, String eventType, java.util.function.Consumer<Appointment> action) {
        Appointment appointment = getAppointment(id);
        try { action.accept(appointment); }
        catch (IllegalStateException exception) { throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception); }
        if ("APPOINTMENT_NO_SHOW".equals(eventType)) {
            slotRepository.deleteByAppointmentId(id); resourceSlotRepository.deleteByAppointmentId(id);
        }
        emit(eventType, appointment);
        return appointment;
    }

    @Transactional
    public List<Appointment> bookRecurring(RecurringAppointmentRequest request) {
        UUID recurrenceGroup = UUID.randomUUID();
        List<Appointment> created = new ArrayList<>();
        for (int index = 0; index < request.occurrences(); index++) {
            LocalDateTime start = request.startsAt().plusDays((long) index * request.intervalDays());
            Appointment appointment = mapper.toAppointment(new AppointmentRequest(request.patientId(), request.doctorId(), start, request.reason(), request.durationMinutes()));
            appointment.assignOperationalDetails(request.appointmentType() == null ? "GENERAL" : request.appointmentType(), request.locationId(), request.roomId(), recurrenceGroup);
            repository.saveAppointment(appointment);
            allocateSlots(appointment, request.resourceIds() == null ? Set.of() : request.resourceIds());
            emit("APPOINTMENT_BOOKED", appointment);
            created.add(appointment);
        }
        return created;
    }

    public AppointmentResource registerResource(AppointmentResourceRequest request) {
        String type = request.resourceType().toUpperCase();
        if (!Set.of("LOCATION", "ROOM", "EQUIPMENT").contains(type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported appointment resource type");
        }
        return resourceRepository.save(new AppointmentResource(UUID.randomUUID(), type, request.name(), request.locationId()));
    }

    public List<AppointmentResource> listResources() { return resourceRepository.findAll(); }

    public WaitlistEntry joinWaitlist(WaitlistRequest request) {
        return repository.saveWaitlistEntry(mapper.toWaitlistEntry(request));
    }

    public List<WaitlistEntry> listWaitlist() {
        return repository.findWaitlist().stream().sorted(Comparator.comparing(WaitlistEntry::createdAt)).toList();
    }

    @Transactional
    public Appointment promoteWaitlist(UUID waitlistId, WaitlistPromotionRequest request) {
        WaitlistEntry entry = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Waitlist entry not found"));
        Appointment appointment = mapper.toAppointment(new AppointmentRequest(entry.patientId(), entry.doctorId(), request.startsAt(), entry.reason(), request.durationMinutes()));
        repository.saveAppointment(appointment);
        allocateSlots(appointment, request.resourceIds() == null ? Set.of() : request.resourceIds());
        try { entry.promote(appointment.id()); }
        catch (IllegalStateException exception) { throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception); }
        emit("WAITLIST_PROMOTED", appointment);
        return appointment;
    }

    public VirtualConsultation createVirtualConsultation(UUID appointmentId, VirtualConsultationRequest request) {
        return repository.saveVirtualConsultation(mapper.toVirtualConsultation(getAppointment(appointmentId), request));
    }

    public List<VirtualConsultation> listVirtualConsultations() {
        return repository.findVirtualConsultations().stream().sorted(Comparator.comparing(VirtualConsultation::createdAt)).toList();
    }

    public ConsentForm captureConsent(UUID appointmentId, ConsentFormRequest request) {
        return repository.saveConsentForm(mapper.toConsentForm(getAppointment(appointmentId), request));
    }

    public List<ConsentForm> listConsentForms() {
        return repository.findConsentForms().stream().sorted(Comparator.comparing(ConsentForm::signedAt)).toList();
    }

    private Appointment getAppointment(UUID id) {
        return repository.findAppointmentById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    private void emit(String eventType, Appointment appointment) {
        Map<String,Object> event = new LinkedHashMap<>();
        event.put("eventId", UUID.randomUUID()); event.put("schemaVersion", 1); event.put("eventType", eventType);
        event.put("occurredAt", Instant.now()); event.put("source", "appointment-service");
        event.put("aggregateType", "APPOINTMENT"); event.put("aggregateId", appointment.id());
        event.put("patientId", appointment.patientId()); event.put("doctorId", appointment.doctorId());
        event.put("status", appointment.status());
        outbox.append("appointment.events.v1", appointment.id().toString(), event);
    }
}
