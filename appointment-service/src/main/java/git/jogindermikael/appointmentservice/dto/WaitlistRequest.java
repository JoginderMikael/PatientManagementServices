package git.jogindermikael.appointmentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record WaitlistRequest(@NotNull UUID patientId, @NotNull UUID doctorId,
        @NotNull LocalDate preferredDate, @NotBlank String reason) {
}
