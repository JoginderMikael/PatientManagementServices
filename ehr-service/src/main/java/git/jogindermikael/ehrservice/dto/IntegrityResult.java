package git.jogindermikael.ehrservice.dto;

import java.util.UUID;

public record IntegrityResult(UUID resourceId, boolean valid, Integer failedVersion,
        int versionsChecked) {
}
