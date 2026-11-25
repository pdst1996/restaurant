package com.ps.restaurant.service;

import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.nio.charset.StandardCharsets;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ps.restaurant.config.Permissions;
import com.ps.restaurant.dto.UserUpdateRequest;
import com.ps.restaurant.dto.ErrorResponseException;
import com.ps.restaurant.model.Role;

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
        return userRepository.findAll(UserSpecifications.withFilters(filter),
                        PageRequest.of(filter.page() == null ? 0 : filter.page(),
                                filter.size() == null ? 50 : filter.size(), Sort.by("id"))).stream()
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
        user.setRole(assignableRole(request.role().id()));
        user.setPassword(encoded(request.password()));
        user.setActive(true);
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        roleRepository.lockAdminRole();
        User user = ServiceSupport.requireById(userRepository, id, "User");
        checkGrant(user.getRole());
        Role next = Objects.equals(user.getRole().getId(), request.role().id())
                ? user.getRole() : assignableRole(request.role().id());
        if (user.isActive() && "admin".equals(user.getRole().getName())
                && (!request.active() || !"admin".equals(next.getName()))
                && userRepository.countByRoleNameAndActiveTrue("admin") <= 1) {
            throw new ConflictException("At least one active administrator is required");
        }
        for (String identifier : List.of(request.email(), request.user())) {
            if (userRepository.findByUserOrEmail(identifier).filter(other -> !id.equals(other.getId())).isPresent()) {
                throw new ConflictException("Email or username already exists");
            }
        }
        user.setEmail(request.email());
        user.setUser(request.user());
        user.setFullName(request.fullName());
        user.setRole(next);
        user.setActive(request.active());
        if (request.password() != null) user.setPassword(encoded(request.password()));
        return UserResponse.from(user);
    }

    private String encoded(String raw) {
        if (raw.isBlank() || raw.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ErrorResponseException(422, "INVALID_PASSWORD", "The password must be nonblank and at most 72 UTF-8 bytes");
        }
        return passwordEncoder.encode(raw);
    }

    private Role assignableRole(Long id) {
        Role role = ServiceSupport.requireById(roleRepository, id, "Role");
        checkGrant(role);
        if (!role.isActive()) throw new ConflictException("Cannot assign an inactive role");
        return role;
    }

    private void checkGrant(Role role) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || Permissions.has(auth, "ROLE_ADMIN")) return;
        if ("admin".equals(role.getName()) || role.getPermissions().stream().anyMatch(p -> !Permissions.has(auth, p))) {
            throw new AccessDeniedException("Insufficient permissions to manage this role");
        }
    }
}

