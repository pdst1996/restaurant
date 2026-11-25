package com.ps.restaurant.config;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.ps.restaurant.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/** Authenticates requests with bearer tokens without creating an HTTP session. */
@Component 
@RequiredArgsConstructor 
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository users;

    /**
     * Resolves current grants from the database on every authenticated request
     * only when no authentication is already present, then continues the chain.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtService.isValid(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                String email = jwtService.extractEmail(token);
                users.findByEmail(email).filter(u -> u.isActive() && u.getRole().isActive()).ifPresent(u -> {
                    var authorities = new ArrayList<>(u.getRole().getPermissions().stream()
                            .map(SimpleGrantedAuthority::new).toList());
                    if ("admin".equals(u.getRole().getName())) authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(email, null, authorities));
                });
            }
        }
        chain.doFilter(request, response);
    }
}
