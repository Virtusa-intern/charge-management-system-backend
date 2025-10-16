package com.bank.charge_management_system.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for login request
 */
public class LoginRequest {
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
}