package com.ps.restaurant.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ps.restaurant.model.User;
import com.ps.restaurant.repository.UserRepository;
import com.ps.restaurant.dto.ApiException.UnauthorizedException;
import com.ps.restaurant.dto.ApiResponse;
import com.ps.restaurant.dto.UserResponse;

import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping ("/api")
@RequiredArgsConstructor 
public class MeController {

    private final UserRepository userRepository;

    @GetMapping ("/me")
    public ApiResponse<UserResponse> me(Authentication auth) {
        User u = userRepository.findByUserOrEmail(auth.getName())
            .orElseThrow(() -> new UnauthorizedException("Sesión inválida"));
        return ApiResponse.ok(new UserResponse(u.getId(), u.getEmail(), u.getUser(), u.getName(), u.getSurName()));
    }
}