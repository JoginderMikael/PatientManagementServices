package git.jogindermikael.patientportalservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record Grant(@NotBlank @Size(max = 200) String proxySubject,
        @NotNull @Pattern(regexp = "RECORDS|APPOINTMENTS|PAYMENTS") String scope,
        @NotNull @Future Instant expiresAt) {
}
