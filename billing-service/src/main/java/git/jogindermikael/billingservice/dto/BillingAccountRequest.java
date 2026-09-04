package git.jogindermikael.billingservice.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record BillingAccountRequest(@NotNull UUID patientId) {}
