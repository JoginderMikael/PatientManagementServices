package git.jogindermikael.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;

public record DeliveryCallbackRequest(@NotBlank String providerMessageId,
        @NotBlank String status, String detail) {
}
