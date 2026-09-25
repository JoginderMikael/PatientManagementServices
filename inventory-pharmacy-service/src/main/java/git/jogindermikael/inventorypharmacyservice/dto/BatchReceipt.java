package git.jogindermikael.inventorypharmacyservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record BatchReceipt(@NotNull UUID medicationId, @NotBlank String lot,
        @NotNull @Future LocalDate expiresOn, @Min(1) int quantity, @NotBlank String reference) {
}
