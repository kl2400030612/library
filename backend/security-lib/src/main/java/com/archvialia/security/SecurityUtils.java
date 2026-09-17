package com.archvialia.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static JwtAuthenticationFilter.AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtAuthenticationFilter.AuthenticatedUser user)) {
            throw new IllegalStateException("Authenticated user is required");
        }
        return user;
    }
}
