package git.jogindermikael.patientportalservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public final class PatientPortalDtos {
    private PatientPortalDtos() {
    }

    public record PortalAppointmentCommand(@NotNull UUID patientId, @NotBlank String preferredSpecialty, @NotBlank String reason) {}
    public record RecordAccessCommand(@NotNull UUID patientId, @NotBlank String recordType) {}
    public record PortalPaymentCommand(@NotNull UUID patientId, @NotNull UUID invoiceId, @NotNull @DecimalMin("0.00") BigDecimal amount) {}
}
