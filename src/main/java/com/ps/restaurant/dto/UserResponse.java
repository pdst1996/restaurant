package com.ps.restaurant.dto;


public class UserResponse {

    public UserResponse(String id, String email, String user, String name, String surName) {
        this.id = id;
        this.email = email;
        this.user = user;
        this.name = name;
        this.surName = surName;
    }

    public UserResponse(String id, String name, String email) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    private String id;
    private String email;
    private String user;
    private String name;
    private String surName;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSurName() {
        return surName;
    }
    public void setSurName(String surName) {
        this.surName = surName;
    }
}
