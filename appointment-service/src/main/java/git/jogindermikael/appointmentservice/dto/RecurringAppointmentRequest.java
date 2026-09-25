package git.jogindermikael.appointmentservice.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record RecurringAppointmentRequest(@NotNull UUID patientId, @NotNull UUID doctorId,
        @NotNull @FutureOrPresent LocalDateTime startsAt, @NotBlank String reason,
        @Min(5) @Max(480) Integer durationMinutes, @Min(2) @Max(52) int occurrences,
        @Min(1) @Max(365) int intervalDays, String appointmentType, UUID locationId, UUID roomId,
        Set<UUID> resourceIds) {
}
