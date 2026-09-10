package git.jogindermikael.authservice.controller;

import git.jogindermikael.authservice.model.User;
import git.jogindermikael.authservice.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {
    private final UserService users;

    public UserAdminController(UserService users) {
        this.users = users;
    }

    public record CreateUserRequest(
            @Email @NotBlank @Size(max = 255) String email,
            @NotBlank @Size(min = 12, max = 200) String password,
            @NotBlank String role) {}

    public record RoleRequest(@NotBlank String role) {}

    public record StatusRequest(@NotBlank String status) {}

    public record UserResponse(UUID id, String email, String role, String status) {
        static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.getStatus());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return UserResponse.from(users.create(request.email(), request.password(), request.role()));
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable UUID id) {
        return UserResponse.from(users.require(id));
    }

    @PatchMapping("/{id}/role")
    public UserResponse role(@PathVariable UUID id, @Valid @RequestBody RoleRequest request) {
        return UserResponse.from(users.changeRole(id, request.role()));
    }

    @PatchMapping("/{id}/status")
    public UserResponse status(@PathVariable UUID id, @Valid @RequestBody StatusRequest request) {
        return UserResponse.from(users.changeStatus(id, request.status()));
    }
}
