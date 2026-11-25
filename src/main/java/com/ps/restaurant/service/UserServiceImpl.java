package com.ps.restaurant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.dto.ErrorResponseException.ConflictException;
import com.ps.restaurant.dto.UserRequest;
import com.ps.restaurant.dto.UserResponse;
import com.ps.restaurant.dto.filters.FilterUserRequest;
import com.ps.restaurant.interfaces.UserService;
import com.ps.restaurant.model.User;
import com.ps.restaurant.repository.RoleRepository;
import com.ps.restaurant.repository.UserRepository;
import com.ps.restaurant.repository.specification.UserSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<UserResponse> filterUsers(FilterUserRequest filter) {
        return userRepository.findAll(UserSpecifications.withFilters(filter)).stream()
                .map(UserResponse::from)
                .toList();
    }

    @Override
    public long countUsers(FilterUserRequest filter) {
        return userRepository.count(UserSpecifications.withFilters(filter));
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.findByUserOrEmail(request.email()).isPresent()
                || userRepository.findByUserOrEmail(request.user()).isPresent()) {
            throw new ConflictException("Email or username already exists");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setUser(request.user());
        user.setFullName(request.fullName());
        user.setRole(ServiceSupport.requireById(roleRepository,
                request.role() == null ? null : request.role().id(), "Role"));
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setActive(true);
        return UserResponse.from(userRepository.save(user));
    }
}

