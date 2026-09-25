package git.jogindermikael.inventorypharmacyservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record Bill(@NotNull @Pattern(regexp = "[A-Z]{3}") String currency) {
}
