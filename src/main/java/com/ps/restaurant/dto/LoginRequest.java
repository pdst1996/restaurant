package com.ps.restaurant.dto;

/** Holds a username-or-email identifier and password; Java generates the constructor and component accessors. */
public record LoginRequest(String user, String password) {

}
