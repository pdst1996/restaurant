package com.ps.restaurant.dto;

import com.ps.restaurant.model.Role;

/** Exposes account identity, role, and activation details without including a password. */
public class UserResponse {

    /** Copies the supplied account details into the response profile. */
    public UserResponse(Long id, String email, String user, String fullName, Role role, boolean active) {
        this.id = id;
        this.email = email;
        this.user = user;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }

    private Long id;
    private String email;
    private String user;
    private String fullName;
    private Role role;
    private boolean active;

    /** Returns the account holder's display name. */
    public String getFullName() {
        return fullName;
    }

    /** Replaces the display name exposed in the response. */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /** Returns the role associated with this account. */
    public Role getRole() {
        return role;
    }

    /** Replaces the role associated with the response profile. */
    public void setRole(Role role) {
        this.role = role;
    }

    /** Returns the account's activation flag. */
    public boolean isActive() {
        return active;
    }

    /** Updates the activation flag exposed in the response. */
    public void setActive(boolean active) {
        this.active = active;
    }

    /** Returns the persisted user identifier. */
    public Long getId() {
        return id;
    }

    /** Updates the user identifier exposed in the response. */
    public void setId(Long id) {
        this.id = id;
    }

    /** Returns the account's email address. */
    public String getEmail() {
        return email;
    }

    /** Replaces the email address exposed in the response. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Returns the account's username. */
    public String getUser() {
        return user;
    }

    /** Replaces the username exposed in the response. */
    public void setUser(String user) {
        this.user = user;
    }
}
