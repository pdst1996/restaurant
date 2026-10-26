package com.ps.restaurant.dto;

/** Returns the issued JWT and user profile through automatically generated record accessors. */
public record LoginResponse (String token, UserResponse user) {

}
