package com.ps.restaurant.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ps.restaurant.dto.UserRequest;
import com.ps.restaurant.dto.UserResponse;
import com.ps.restaurant.dto.filters.FilterUserRequest;
import com.ps.restaurant.interfaces.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id,
            @Valid @RequestBody com.ps.restaurant.dto.UserUpdateRequest request) {
        return userService.updateUser(id, request);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers(
            @Valid @ModelAttribute FilterUserRequest filter) {
        return ResponseEntity.ok(userService.filterUsers(filter));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getUsersCount(@Valid @ModelAttribute FilterUserRequest filter) {
        return ResponseEntity.ok(userService.countUsers(filter));
    }
}

