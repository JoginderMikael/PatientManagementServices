package git.jogindermikael.appointmentservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public final class AppointmentModels {
    private AppointmentModels() {
    }

    @Entity
    @Table(name = "doctor_schedule")
    public static class DoctorSchedule {
        @Id
        private UUID id;
        @Column(nullable = false)
        private UUID doctorId;
        @Column(nullable = false)
        private LocalDate workDate;
        @Column(nullable = false)
        private LocalTime startsAt;
        @Column(nullable = false)
        private LocalTime endsAt;
        @Column(nullable = false)
        private String location;
        @Version
        private long version;

        protected DoctorSchedule() {
        }

        public DoctorSchedule(UUID id, UUID doctorId, LocalDate workDate, LocalTime startsAt, LocalTime endsAt,
                String location) {
            this.id = id;
            this.doctorId = doctorId;
            this.workDate = workDate;
            this.startsAt = startsAt;
            this.endsAt = endsAt;
            this.location = location;
        }

        public UUID id() {
            return id;
        }

        public UUID getId() {
            return id;
        }

        public UUID doctorId() {
            return doctorId;
        }

        public UUID getDoctorId() {
            return doctorId;
        }

        public LocalDate workDate() {
            return workDate;
        }

        public LocalDate getWorkDate() {
            return workDate;
        }

        public LocalTime startsAt() {
            return startsAt;
        }

        public LocalTime getStartsAt() {
            return startsAt;
        }

        public LocalTime endsAt() {
            return endsAt;
        }

        public LocalTime getEndsAt() {
            return endsAt;
        }

        public String location() {
            return location;
        }

        public String getLocation() {
            return location;
        }

        public long getVersion() {
            return version;
        }
    }

    @Entity
    @Table(name = "appointment")
    public static class Appointment {
        @Id
        private UUID id;
        @Column(nullable = false)
        private UUID patientId;
        @Column(nullable = false)
        private UUID doctorId;
        @Column(nullable = false)
        private LocalDateTime startsAt;
        @Column(nullable = false)
        private LocalDateTime endsAt;
        @Column(nullable = false)
        private String reason;
        @Column(nullable = false, length = 24)
        private String status;
        private String cancellationReason;
        @Column(nullable = false)
        private String appointmentType = "GENERAL";
        private UUID locationId;
        private UUID roomId;
        private UUID recurrenceGroupId;
        private Instant checkedInAt;
        private Instant checkedOutAt;
        private Instant noShowAt;
        @Column(nullable = false)
        private Instant updatedAt;
        @Version
        private long version;

        protected Appointment() {
        }

        public Appointment(UUID id, UUID patientId, UUID doctorId, LocalDateTime startsAt, LocalDateTime endsAt,
                String reason, String status, Instant updatedAt) {
            this.id = id;
            this.patientId = patientId;
            this.doctorId = doctorId;
            this.startsAt = startsAt;
            this.endsAt = endsAt;
            this.reason = reason;
            this.status = status;
            this.updatedAt = updatedAt;
        }

        public UUID id() {
            return id;
        }

        public UUID getId() {
            return id;
        }

        public UUID patientId() {
            return patientId;
        }

        public UUID getPatientId() {
            return patientId;
        }

        public UUID doctorId() {
            return doctorId;
        }

        public UUID getDoctorId() {
            return doctorId;
        }

        public LocalDateTime startsAt() {
            return startsAt;
        }

        public LocalDateTime getStartsAt() {
            return startsAt;
        }

        public LocalDateTime endsAt() {
            return endsAt;
        }

        public LocalDateTime getEndsAt() {
            return endsAt;
        }

        public String reason() {
            return reason;
        }

        public String getReason() {
            return reason;
        }

        public String status() {
            return status;
        }

        public String getStatus() {
            return status;
        }

        public String getCancellationReason() {
            return cancellationReason;
        }

        public String getAppointmentType() {
            return appointmentType;
        }

        public UUID getLocationId() {
            return locationId;
        }

        public UUID getRoomId() {
            return roomId;
        }

        public UUID getRecurrenceGroupId() {
            return recurrenceGroupId;
        }

        public Instant getCheckedInAt() {
            return checkedInAt;
        }

        public Instant getCheckedOutAt() {
            return checkedOutAt;
        }

        public Instant getNoShowAt() {
            return noShowAt;
        }

        public Instant updatedAt() {
            return updatedAt;
        }

        public Instant getUpdatedAt() {
            return updatedAt;
        }

        public long getVersion() {
            return version;
        }

        public void cancel(String reason) {
            status = "CANCELLED";
            cancellationReason = reason;
            updatedAt = Instant.now();
        }

        public void reschedule(LocalDateTime start, int durationMinutes) {
            if (!java.util.Set.of("BOOKED", "RESCHEDULED").contains(status))
                throw new IllegalStateException("Appointment cannot be rescheduled");
            startsAt = start;
            endsAt = start.plusMinutes(durationMinutes);
            status = "RESCHEDULED";
            updatedAt = Instant.now();
        }

        public void checkIn() {
            if (!java.util.Set.of("BOOKED", "RESCHEDULED").contains(status))
                throw new IllegalStateException("Appointment cannot be checked in");
            status = "CHECKED_IN";
            checkedInAt = Instant.now();
            updatedAt = checkedInAt;
        }

        public void checkOut() {
            if (!"CHECKED_IN".equals(status))
                throw new IllegalStateException("Appointment must be checked in first");
            status = "COMPLETED";
            checkedOutAt = Instant.now();
            updatedAt = checkedOutAt;
        }

        public void markNoShow() {
            if (!java.util.Set.of("BOOKED", "RESCHEDULED").contains(status))
                throw new IllegalStateException("Appointment cannot be marked no-show");
            status = "NO_SHOW";
            noShowAt = Instant.now();
            updatedAt = noShowAt;
        }

        public void assignOperationalDetails(String type, UUID location, UUID room, UUID recurrence) {
            appointmentType = type;
            locationId = location;
            roomId = room;
            recurrenceGroupId = recurrence;
            updatedAt = Instant.now();
        }
    }

    @Entity
    @Table(name = "appointment_slot")
    public static class AppointmentSlot {
        @Id
        private UUID id;
        @Column(nullable = false)
        private UUID appointmentId;
        @Column(nullable = false)
        private UUID doctorId;
        @Column(nullable = false)
        private LocalDateTime slotTime;

        protected AppointmentSlot() {
        }

        public AppointmentSlot(UUID appointmentId, UUID doctorId, LocalDateTime slotTime) {
            id = UUID.randomUUID();
            this.appointmentId = appointmentId;
            this.doctorId = doctorId;
            this.slotTime = slotTime;
        }

        public UUID getId() {
            return id;
        }

        public UUID getAppointmentId() {
            return appointmentId;
        }

        public UUID getDoctorId() {
            return doctorId;
        }

        public LocalDateTime getSlotTime() {
            return slotTime;
        }
    }

    @Entity
    @Table(name = "appointment_waitlist")
    public static class WaitlistEntry {
        @Id
        private UUID id;
        @Column(nullable = false)
        private UUID patientId;
        @Column(nullable = false)
        private UUID doctorId;
        @Column(nullable = false)
        private LocalDate preferredDate;
        @Column(nullable = false)
        private String reason;
        @Column(nullable = false)
        private Instant createdAt;
        @Column(nullable = false)
        private String status = "WAITING";
        private Instant offeredAt;
        private UUID promotedAppointmentId;
        @Version
        private long version;

        protected WaitlistEntry() {
        }

        public WaitlistEntry(UUID id, UUID patientId, UUID doctorId, LocalDate preferredDate, String reason,
                Instant createdAt) {
            this.id = id;
            this.patientId = patientId;
            this.doctorId = doctorId;
            this.preferredDate = preferredDate;
            this.reason = reason;
            this.createdAt = createdAt;
        }

        public UUID id() {
            return id;
        }

        public UUID getId() {
            return id;
        }

        public UUID patientId() {
            return patientId;
        }

        public UUID getPatientId() {
            return patientId;
        }

        public UUID doctorId() {
            return doctorId;
        }

        public UUID getDoctorId() {
            return doctorId;
        }

        public LocalDate preferredDate() {
            return preferredDate;
        }

        public LocalDate getPreferredDate() {
            return preferredDate;
        }

        public String reason() {
            return reason;
        }

        public String getReason() {
            return reason;
        }

        public Instant createdAt() {
            return createdAt;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public String getStatus() {
            return status;
        }

        public Instant getOfferedAt() {
            return offeredAt;
        }

        public UUID getPromotedAppointmentId() {
            return promotedAppointmentId;
        }

        public long getVersion() {
            return version;
        }

        public void promote(UUID appointmentId) {
            if (!"WAITING".equals(status))
                throw new IllegalStateException("Waitlist entry is not waiting");
            status = "PROMOTED";
            offeredAt = Instant.now();
            promotedAppointmentId = appointmentId;
        }
    }

    @Entity
    @Table(name = "appointment_resource")
    public static class AppointmentResource {
        @Id
        private UUID id;
        @Column(nullable = false)
        private String resourceType;
        @Column(nullable = false)
        private String name;
        private UUID locationId;
        @Column(nullable = false)
        private boolean active = true;
        @Version
        private long version;

        protected AppointmentResource() {
        }

        public AppointmentResource(UUID id, String resourceType, String name, UUID locationId) {
            this.id = id;
            this.resourceType = resourceType;
            this.name = name;
            this.locationId = locationId;
        }

        public UUID getId() {
            return id;
        }

        public String getResourceType() {
            return resourceType;
        }

        public String getName() {
            return name;
        }

        public UUID getLocationId() {
            return locationId;
        }

        public boolean isActive() {
            return active;
        }

        public long getVersion() {
            return version;
        }
    }

    @Entity
    @Table(name = "appointment_resource_slot")
    public static class AppointmentResourceSlot {
        @Id
        private UUID id;
        @Column(nullable = false)
        private UUID appointmentId;
        @Column(nullable = false)
        private UUID resourceId;
        @Column(nullable = false)
        private LocalDateTime slotTime;

        protected AppointmentResourceSlot() {
        }

        public AppointmentResourceSlot(UUID appointmentId, UUID resourceId, LocalDateTime slotTime) {
            id = UUID.randomUUID();
            this.appointmentId = appointmentId;
            this.resourceId = resourceId;
            this.slotTime = slotTime;
        }

        public UUID getId() {
            return id;
        }

        public UUID getAppointmentId() {
            return appointmentId;
        }

        public UUID getResourceId() {
            return resourceId;
        }

        public LocalDateTime getSlotTime() {
            return slotTime;
        }
    }

    @Entity
    @Table(name = "virtual_consultation")
    public static class VirtualConsultation {
        @Id
        private UUID id;
        @Column(nullable = false)
        private UUID appointmentId;
        @Column(nullable = false)
        private String provider;
        @Column(nullable = false)
        private String joinUrl;
        @Column(nullable = false)
        private String status;
        @Column(nullable = false)
        private Instant createdAt;

        protected VirtualConsultation() {
        }

        public VirtualConsultation(UUID id, UUID appointmentId, String provider, String joinUrl, String status,
                Instant createdAt) {
            this.id = id;
            this.appointmentId = appointmentId;
            this.provider = provider;
            this.joinUrl = joinUrl;
            this.status = status;
            this.createdAt = createdAt;
        }

        public UUID id() {
            return id;
        }

        public UUID getId() {
            return id;
        }

        public UUID appointmentId() {
            return appointmentId;
        }

        public UUID getAppointmentId() {
            return appointmentId;
        }

        public String provider() {
            return provider;
        }

        public String getProvider() {
            return provider;
        }

        public String joinUrl() {
            return joinUrl;
        }

        public String getJoinUrl() {
            return joinUrl;
        }

        public String status() {
            return status;
        }

        public String getStatus() {
            return status;
        }

        public Instant createdAt() {
            return createdAt;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }
    }

    @Entity
    @Table(name = "appointment_consent")
    public static class ConsentForm {
        @Id
        private UUID id;
        @Column(nullable = false)
        private UUID appointmentId;
        @Column(nullable = false)
        private UUID patientId;
        @Column(nullable = false)
        private String formType;
        @Column(nullable = false, columnDefinition = "TEXT")
        private String signature;
        @Column(nullable = false)
        private Instant signedAt;

        protected ConsentForm() {
        }

        public ConsentForm(UUID id, UUID appointmentId, UUID patientId, String formType, String signature,
                Instant signedAt) {
            this.id = id;
            this.appointmentId = appointmentId;
            this.patientId = patientId;
            this.formType = formType;
            this.signature = signature;
            this.signedAt = signedAt;
        }

        public UUID id() {
            return id;
        }

        public UUID getId() {
            return id;
        }

        public UUID appointmentId() {
            return appointmentId;
        }

        public UUID getAppointmentId() {
            return appointmentId;
        }

        public UUID patientId() {
            return patientId;
        }

        public UUID getPatientId() {
            return patientId;
        }

        public String formType() {
            return formType;
        }

        public String getFormType() {
            return formType;
        }

        public String signature() {
            return signature;
        }

        public String getSignature() {
            return signature;
        }

        public Instant signedAt() {
            return signedAt;
        }

        public Instant getSignedAt() {
            return signedAt;
        }
    }
}
