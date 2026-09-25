package git.jogindermikael.inventorypharmacyservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SafetyReview(@NotNull UUID medicationId, boolean allergiesChecked,
        boolean interactionsChecked, boolean doseChecked, @NotBlank String reason) {
}
