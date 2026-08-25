package git.jogindermikael.auditcomplianceservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AuditEventRequest(@NotNull UUID actorId, @NotBlank String actorRole, @NotBlank String action, @NotNull UUID patientId, @NotBlank String resourceType, @NotNull UUID resourceId, @NotBlank String sourceService, @NotBlank String reason) {}
