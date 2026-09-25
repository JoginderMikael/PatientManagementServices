package git.jogindermikael.appointmentservice.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record WaitlistPromotionRequest(@NotNull @FutureOrPresent LocalDateTime startsAt,
        @Min(5) @Max(480) Integer durationMinutes, Set<UUID> resourceIds) {
}
