package com.ps.restaurant.config;

import java.security.Principal;
import java.util.Optional;
import org.springframework.security.core.Authentication;

public record SessionPrincipal(Long id) implements Principal {
    @Override
    public String getName() { return id.toString(); }

    public static Optional<Long> userId(Authentication auth) {
        return auth != null && auth.getPrincipal() instanceof SessionPrincipal principal
                ? Optional.of(principal.id()) : Optional.empty();
    }
}
