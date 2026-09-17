package com.archvialia.auth;

import com.archvialia.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
    private final JwtService jwtService;
    public JwtTokenService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration-ms:3600000}") long expirationMs) {
        this.jwtService = new JwtService(secret, expirationMs);
    }
    public String create(User user) {
        String role = user.getRole() == Role.USER ? Role.STUDENT.name() : user.getRole().name();
        return jwtService.generateToken(user.getId(), user.getEmail(), role);
    }
}
