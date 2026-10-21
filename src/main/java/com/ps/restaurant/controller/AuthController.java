package com.ps.restaurant.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ps.restaurant.config.JwtService;
import com.ps.restaurant.dto.ApiError;
import com.ps.restaurant.dto.LoginRequest;
import com.ps.restaurant.dto.LoginResponse;
import com.ps.restaurant.dto.UserRequest;
import com.ps.restaurant.dto.UserResponse;
import com.ps.restaurant.model.User;
import com.ps.restaurant.repository.UserRepository;
import com.ps.restaurant.service.ErrorBuilder;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/register")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "object"))),
            @ApiResponse(responseCode = "409", description = "El correo o el usuario ya existe",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<?> register(@RequestBody UserRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ErrorBuilder("AUTH_EMAIL_ALREADY_EXISTS", "AuthController.register")
                    .statusCode(HttpStatus.CONFLICT)
                    .createException();
        }
        if (userRepository.findByUserOrEmail(req.user()).isPresent()) {
            throw new ErrorBuilder("AUTH_USER_ALREADY_EXISTS", "AuthController.register")
                    .statusCode(HttpStatus.CONFLICT)
                    .createException();
        }
        User user = new User();
        user.setName(req.name());
        user.setSurName(req.surName());
        user.setEmail(req.email());
        user.setUser(req.user());
        user.setPassword(passwordEncoder.encode(req.password()));
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Usuario creado"));
    }

    @PostMapping("/login")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuario inexistente o contraseña incorrecta",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        User user = userRepository.findByUserOrEmail(req.user())
                .orElseThrow(() -> new ErrorBuilder("AUTH_USER_NOT_FOUND", "AuthController.login")
                        .statusCode(HttpStatus.UNAUTHORIZED)
                        .createException());
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new ErrorBuilder("AUTH_INVALID_PASSWORD", "AuthController.login")
                    .statusCode(HttpStatus.UNAUTHORIZED)
                    .createException();
        }
        return ResponseEntity.ok(new LoginResponse(
                jwtService.generateToken(user.getEmail()),
                new UserResponse(user.getId(), user.getEmail(), user.getUser(), user.getName(), user.getSurName())));
    }
    
}
