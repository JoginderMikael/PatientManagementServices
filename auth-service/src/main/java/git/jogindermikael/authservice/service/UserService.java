package git.jogindermikael.authservice.service;


import git.jogindermikael.authservice.model.User;
import git.jogindermikael.authservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public static final Set<String> ROLES = Set.of(
            "ADMIN", "PATIENT", "CLINICIAN", "NURSE", "REGISTRATION_STAFF",
            "RECEPTIONIST", "BILLING_STAFF", "PHARMACIST", "LAB_STAFF",
            "PRIVACY_OFFICER", "AUDITOR", "ANALYST");
    public static final Set<String> STATUSES = Set.of("INVITED", "ACTIVE", "LOCKED", "DISABLED");

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByEmail(String email){

        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public User require(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional
    public User create(String email, String password, String role) {
        String normalizedEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
        String normalizedRole = normalizeRole(role);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already belongs to a user");
        }
        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(normalizedRole);
        user.setStatus("ACTIVE");
        return userRepository.saveAndFlush(user);
    }

    @Transactional
    public User changeRole(UUID id, String role) {
        User user = require(id);
        user.setRole(normalizeRole(role));
        return user;
    }

    @Transactional
    public User changeStatus(UUID id, String status) {
        String normalized = status.trim().toUpperCase(java.util.Locale.ROOT);
        if (!STATUSES.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported user status");
        }
        User user = require(id);
        user.setStatus(normalized);
        return user;
    }

    private String normalizeRole(String role) {
        String normalized = role.trim().toUpperCase(java.util.Locale.ROOT);
        if (!ROLES.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported user role");
        }
        return normalized;
    }
}
