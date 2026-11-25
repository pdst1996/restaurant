package com.ps.restaurant.interfaces;

import java.util.List;
import java.util.Optional;

import com.ps.restaurant.dto.UserRequest;
import com.ps.restaurant.dto.UserResponse;
import com.ps.restaurant.dto.filters.FilterUserRequest;
import com.ps.restaurant.model.User;

public interface UserService {

    Optional<User> getUserById(Long id);

    List<UserResponse> filterUsers(FilterUserRequest filter);

    long countUsers(FilterUserRequest filter);

    UserResponse createUser(UserRequest request);
}

