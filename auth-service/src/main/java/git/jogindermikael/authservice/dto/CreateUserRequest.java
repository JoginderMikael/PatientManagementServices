package git.jogindermikael.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(@Email @NotBlank @Size(max = 255) String email,
        @NotBlank @Size(min = 12, max = 200) String password, @NotBlank String role) {
}
