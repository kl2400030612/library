package com.archvialia.auth;

import com.archvialia.security.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokens;

    public AuthController(UserRepository users, PasswordEncoder passwordEncoder, JwtTokenService tokens) {
        this.users = users; this.passwordEncoder = passwordEncoder; this.tokens = tokens;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role() == null ? Role.STUDENT : request.role());
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.USER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Public registration is limited to Student or Faculty accounts");
        }
        user = users.save(user);
        return AuthResponse.from(user, tokens.create(user));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user = users.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return AuthResponse.from(user, tokens.create(user));
    }

    @GetMapping("/health")
    public String health() { return "auth-service:UP"; }

    /** Librarian/Admin-only user management. */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> allUsers() {
        return users.findAll().stream().map(UserResponse::from).toList();
    }

    @PutMapping("/admin/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        User target = users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (request.role() == Role.USER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "USER is a legacy role; use STUDENT, FACULTY or ADMIN");
        }
        var current = SecurityUtils.currentUser();
        if (current.id().equals(id) && request.role() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot remove your own librarian/admin access");
        }
        target.setRole(request.role());
        return UserResponse.from(users.save(target));
    }

    public record RegisterRequest(
            @NotBlank String name,
            @Email @NotBlank String email,
            @NotBlank @Size(min=8, max=100) String password,
            Role role) {}

    public record LoginRequest(@Email @NotBlank String email, @NotBlank @Size(min=8, max=100) String password) {}

    public record RoleRequest(@jakarta.validation.constraints.NotNull Role role) {}

    public record AuthResponse(Long userId, String name, String email, String role, String token) {
        static AuthResponse from(User u, String token) {
            return new AuthResponse(u.getId(), u.getName(), u.getEmail(), displayRole(u.getRole()), token);
        }
    }

    public record UserResponse(Long userId, String name, String email, String role) {
        static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getName(), u.getEmail(), displayRole(u.getRole()));
        }
    }

    private static String displayRole(Role role) {
        return role == Role.USER ? "STUDENT" : role.name();
    }
}
