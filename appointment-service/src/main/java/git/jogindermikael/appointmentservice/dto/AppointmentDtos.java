package git.jogindermikael.appointmentservice.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Set;

public final class AppointmentDtos {
        private AppointmentDtos() {
        }

        public record DoctorScheduleRequest(UUID id, @NotNull UUID doctorId, @NotNull LocalDate workDate,
                        @NotBlank String startsAt, @NotBlank String endsAt, @NotBlank String location) {
        }

        public record AppointmentRequest(@NotNull UUID patientId, @NotNull UUID doctorId,
                        @NotNull @FutureOrPresent LocalDateTime startsAt, @NotBlank String reason,
                        @Min(5) @Max(480) Integer durationMinutes) {
        }

        public record CancellationRequest(@NotBlank String reason) {
        }

        public record WaitlistRequest(@NotNull UUID patientId, @NotNull UUID doctorId, @NotNull LocalDate preferredDate,
                        @NotBlank String reason) {
        }

        public record VirtualConsultationRequest(@NotBlank String provider, @NotBlank String joinUrl) {
        }

        public record ConsentFormRequest(@NotNull UUID patientId, @NotBlank String formType,
                        @NotBlank String signature) {
        }

        public record RescheduleRequest(@NotNull @FutureOrPresent LocalDateTime startsAt,
                        @Min(5) @Max(480) Integer durationMinutes, Set<UUID> resourceIds) {
        }

        public record RecurringAppointmentRequest(@NotNull UUID patientId, @NotNull UUID doctorId,
                        @NotNull @FutureOrPresent LocalDateTime startsAt, @NotBlank String reason,
                        @Min(5) @Max(480) Integer durationMinutes, @Min(2) @Max(52) int occurrences,
                        @Min(1) @Max(365) int intervalDays, String appointmentType, UUID locationId, UUID roomId,
                        Set<UUID> resourceIds) {
        }

        public record AppointmentResourceRequest(@NotBlank String resourceType, @NotBlank String name,
                        UUID locationId) {
        }

        public record WaitlistPromotionRequest(@NotNull @FutureOrPresent LocalDateTime startsAt,
                        @Min(5) @Max(480) Integer durationMinutes, Set<UUID> resourceIds) {
        }
}
