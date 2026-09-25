package git.jogindermikael.appointmentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record DoctorScheduleRequest(UUID id, @NotNull UUID doctorId, @NotNull LocalDate workDate,
        @NotBlank String startsAt, @NotBlank String endsAt, @NotBlank String location) {
}
