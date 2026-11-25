package com.ps.restaurant.config;

import static com.ps.restaurant.config.Permissions.*;
import static org.springframework.http.HttpMethod.*;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.ps.restaurant.dto.ErrorResponseException;
import com.ps.restaurant.dto.ErrorResponseException.UnauthorizedException;
import jakarta.servlet.DispatcherType;

/** Defines password hashing, browser cross-origin access, and stateless API security. */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Provides BCrypt hashing and password comparison for authentication operations. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Allows the local Vite frontend to call all routes with the configured HTTP methods and headers. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173")); // Local Vite development server.
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Disables CSRF and sessions, enables CORS, leaves authentication and documentation
     * routes public, and requires JWT authentication elsewhere with a 401 entry point.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver errors) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                errors.resolveException(req, res, null, new UnauthorizedException("Authentication required")))
                        .accessDeniedHandler((req, res, ex) ->
                                errors.resolveException(req, res, null, new ErrorResponseException(403, "FORBIDDEN", "Permission denied"))))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(POST, "/auth/login").permitAll()
                        .requestMatchers(GET, "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(GET, "/me").authenticated()
                        .requestMatchers(POST, "/auth/register").hasRole("ADMIN")
                        .requestMatchers("/user/users/**").hasAuthority(USER_MANAGE)
                        .requestMatchers(GET, "/role/roles", "/role/roles/count").hasAnyAuthority(USER_MANAGE, ROLE_MANAGE)
                        .requestMatchers("/role/roles/**").access(AuthorizationManagers.allOf(
                                AuthorityAuthorizationManager.hasRole("ADMIN"), AuthorityAuthorizationManager.hasAuthority(ROLE_MANAGE)))
                        .requestMatchers(POST, "/order/orders/*/status").hasAuthority(ORDER_STATUS)
                        .requestMatchers(POST, "/order/orders/*/complete").hasAuthority(ORDER_COMPLETE)
                        .requestMatchers(POST, "/order/orders/*/cancel").hasAuthority(ORDER_CANCEL)
                        .requestMatchers(GET, "/order/orders/*/balance").hasAuthority(PAYMENT_VIEW)
                        .requestMatchers(GET, "/order/**", "/order-item/**").hasAuthority(ORDER_VIEW)
                        .requestMatchers(POST, "/order/orders").hasAuthority(ORDER_CREATE)
                        .requestMatchers(PUT, "/order/orders/*").hasAuthority(ORDER_EDIT)
                        .requestMatchers("/order-item/**").hasAuthority(ORDER_EDIT)
                        .requestMatchers(POST, "/payment/payments/*/refund").hasAuthority(PAYMENT_REFUND)
                        .requestMatchers(GET, "/payment/**").hasAuthority(PAYMENT_VIEW)
                        .requestMatchers(POST, "/payment/payments").hasAuthority(PAYMENT_CREATE)
                        .requestMatchers(GET, "/item/**", "/category/**", "/allergy/**", "/item-size/**", "/item-attribute/**").hasAuthority(ITEM_VIEW)
                        .requestMatchers("/item/**", "/category/**", "/allergy/**", "/item-size/**", "/item-attribute/**").hasAuthority(ITEM_MANAGE)
                        .requestMatchers(GET, "/floor/**", "/table/**", "/plan-element/**", "/element-type/**").hasAuthority(FLOOR_VIEW)
                        .requestMatchers("/floor/**", "/table/**", "/plan-element/**", "/element-type/**").hasAuthority(FLOOR_MANAGE)
                        .anyRequest().denyAll())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
