package git.jogindermikael.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record BillAlertRequest(@NotNull UUID patientId, @NotNull UUID invoiceId,
        @NotBlank String channel, @NotBlank String destination, @NotBlank String message) {
}
