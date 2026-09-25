package git.jogindermikael.authservice.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleRequest(@NotBlank String role) {
}
