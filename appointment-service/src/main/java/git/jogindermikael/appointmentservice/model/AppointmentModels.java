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
    private AppointmentModels() {}

    @Entity @Table(name = "doctor_schedule")
    public static class DoctorSchedule {
        @Id private UUID id;
        @Column(nullable = false) private UUID doctorId;
        @Column(nullable = false) private LocalDate workDate;
        @Column(nullable = false) private LocalTime startsAt;
        @Column(nullable = false) private LocalTime endsAt;
        @Column(nullable = false) private String location;
        @Version private long version;
        protected DoctorSchedule() {}
        public DoctorSchedule(UUID id, UUID doctorId, LocalDate workDate, LocalTime startsAt, LocalTime endsAt, String location) {
            this.id=id; this.doctorId=doctorId; this.workDate=workDate; this.startsAt=startsAt; this.endsAt=endsAt; this.location=location;
        }
        public UUID id(){return id;} public UUID getId(){return id;}
        public UUID doctorId(){return doctorId;} public UUID getDoctorId(){return doctorId;}
        public LocalDate workDate(){return workDate;} public LocalDate getWorkDate(){return workDate;}
        public LocalTime startsAt(){return startsAt;} public LocalTime getStartsAt(){return startsAt;}
        public LocalTime endsAt(){return endsAt;} public LocalTime getEndsAt(){return endsAt;}
        public String location(){return location;} public String getLocation(){return location;}
        public long getVersion(){return version;}
    }

    @Entity @Table(name = "appointment")
    public static class Appointment {
        @Id private UUID id;
        @Column(nullable = false) private UUID patientId;
        @Column(nullable = false) private UUID doctorId;
        @Column(nullable = false) private LocalDateTime startsAt;
        @Column(nullable = false) private LocalDateTime endsAt;
        @Column(nullable = false) private String reason;
        @Column(nullable = false, length = 24) private String status;
        private String cancellationReason;
        @Column(nullable = false) private Instant updatedAt;
        @Version private long version;
        protected Appointment() {}
        public Appointment(UUID id, UUID patientId, UUID doctorId, LocalDateTime startsAt, LocalDateTime endsAt, String reason, String status, Instant updatedAt) {
            this.id=id; this.patientId=patientId; this.doctorId=doctorId; this.startsAt=startsAt; this.endsAt=endsAt; this.reason=reason; this.status=status; this.updatedAt=updatedAt;
        }
        public UUID id(){return id;} public UUID getId(){return id;}
        public UUID patientId(){return patientId;} public UUID getPatientId(){return patientId;}
        public UUID doctorId(){return doctorId;} public UUID getDoctorId(){return doctorId;}
        public LocalDateTime startsAt(){return startsAt;} public LocalDateTime getStartsAt(){return startsAt;}
        public LocalDateTime endsAt(){return endsAt;} public LocalDateTime getEndsAt(){return endsAt;}
        public String reason(){return reason;} public String getReason(){return reason;}
        public String status(){return status;} public String getStatus(){return status;}
        public String getCancellationReason(){return cancellationReason;}
        public Instant updatedAt(){return updatedAt;} public Instant getUpdatedAt(){return updatedAt;}
        public long getVersion(){return version;}
        public void cancel(String reason) { status="CANCELLED"; cancellationReason=reason; updatedAt=Instant.now(); }
    }

    @Entity @Table(name = "appointment_slot")
    public static class AppointmentSlot {
        @Id private UUID id;
        @Column(nullable = false) private UUID appointmentId;
        @Column(nullable = false) private UUID doctorId;
        @Column(nullable = false) private LocalDateTime slotTime;
        protected AppointmentSlot() {}
        public AppointmentSlot(UUID appointmentId, UUID doctorId, LocalDateTime slotTime) {
            id=UUID.randomUUID(); this.appointmentId=appointmentId; this.doctorId=doctorId; this.slotTime=slotTime;
        }
        public UUID getId(){return id;} public UUID getAppointmentId(){return appointmentId;}
        public UUID getDoctorId(){return doctorId;} public LocalDateTime getSlotTime(){return slotTime;}
    }

    @Entity @Table(name = "appointment_waitlist")
    public static class WaitlistEntry {
        @Id private UUID id;
        @Column(nullable = false) private UUID patientId;
        @Column(nullable = false) private UUID doctorId;
        @Column(nullable = false) private LocalDate preferredDate;
        @Column(nullable = false) private String reason;
        @Column(nullable = false) private Instant createdAt;
        protected WaitlistEntry() {}
        public WaitlistEntry(UUID id,UUID patientId,UUID doctorId,LocalDate preferredDate,String reason,Instant createdAt){this.id=id;this.patientId=patientId;this.doctorId=doctorId;this.preferredDate=preferredDate;this.reason=reason;this.createdAt=createdAt;}
        public UUID id(){return id;} public UUID getId(){return id;} public UUID patientId(){return patientId;} public UUID getPatientId(){return patientId;}
        public UUID doctorId(){return doctorId;} public UUID getDoctorId(){return doctorId;} public LocalDate preferredDate(){return preferredDate;} public LocalDate getPreferredDate(){return preferredDate;}
        public String reason(){return reason;} public String getReason(){return reason;} public Instant createdAt(){return createdAt;} public Instant getCreatedAt(){return createdAt;}
    }

    @Entity @Table(name = "virtual_consultation")
    public static class VirtualConsultation {
        @Id private UUID id;
        @Column(nullable = false) private UUID appointmentId;
        @Column(nullable = false) private String provider;
        @Column(nullable = false) private String joinUrl;
        @Column(nullable = false) private String status;
        @Column(nullable = false) private Instant createdAt;
        protected VirtualConsultation() {}
        public VirtualConsultation(UUID id,UUID appointmentId,String provider,String joinUrl,String status,Instant createdAt){this.id=id;this.appointmentId=appointmentId;this.provider=provider;this.joinUrl=joinUrl;this.status=status;this.createdAt=createdAt;}
        public UUID id(){return id;} public UUID getId(){return id;} public UUID appointmentId(){return appointmentId;} public UUID getAppointmentId(){return appointmentId;}
        public String provider(){return provider;} public String getProvider(){return provider;} public String joinUrl(){return joinUrl;} public String getJoinUrl(){return joinUrl;}
        public String status(){return status;} public String getStatus(){return status;} public Instant createdAt(){return createdAt;} public Instant getCreatedAt(){return createdAt;}
    }

    @Entity @Table(name = "appointment_consent")
    public static class ConsentForm {
        @Id private UUID id;
        @Column(nullable = false) private UUID appointmentId;
        @Column(nullable = false) private UUID patientId;
        @Column(nullable = false) private String formType;
        @Column(nullable = false, columnDefinition = "TEXT") private String signature;
        @Column(nullable = false) private Instant signedAt;
        protected ConsentForm() {}
        public ConsentForm(UUID id,UUID appointmentId,UUID patientId,String formType,String signature,Instant signedAt){this.id=id;this.appointmentId=appointmentId;this.patientId=patientId;this.formType=formType;this.signature=signature;this.signedAt=signedAt;}
        public UUID id(){return id;} public UUID getId(){return id;} public UUID appointmentId(){return appointmentId;} public UUID getAppointmentId(){return appointmentId;}
        public UUID patientId(){return patientId;} public UUID getPatientId(){return patientId;} public String formType(){return formType;} public String getFormType(){return formType;}
        public String signature(){return signature;} public String getSignature(){return signature;} public Instant signedAt(){return signedAt;} public Instant getSignedAt(){return signedAt;}
    }
}
