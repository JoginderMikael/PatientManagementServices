package git.jogindermikael.authservice.dto;

import jakarta.validation.constraints.NotBlank;

public record StatusRequest(@NotBlank String status) {
}
