package git.jogindermikael.patientportalservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AppointmentResolution(
        @NotNull @Pattern(regexp = "SCHEDULED|DECLINED") String status,
        @NotBlank String appointmentReference) {
}
