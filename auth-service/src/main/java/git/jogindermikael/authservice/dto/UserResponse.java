package git.jogindermikael.authservice.dto;

import git.jogindermikael.authservice.model.User;
import java.util.UUID;

public record UserResponse(UUID id, String email, String role, String status) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.getStatus());
    }
}
