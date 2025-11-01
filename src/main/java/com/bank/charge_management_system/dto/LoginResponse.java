package com.bank.charge_management_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for login response containing JWT token and user info
 */
@Schema(description = "Response containing JWT token and authenticated user information")
public class LoginResponse {

    @Schema(description = "JWT access token for authentication", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTYzOTU4...")
    private String token;

    @Schema(description = "Token type (always Bearer)", example = "Bearer", defaultValue = "Bearer")
    private String type = "Bearer";

    @Schema(description = "User's unique ID", example = "1")
    private Long id;

    @Schema(description = "User's username", example = "admin")
    private String username;

    @Schema(description = "User's email address", example = "admin@example.com")
    private String email;

    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @Schema(description = "User's role in the system", example = "ADMIN", allowableValues = { "ADMIN", "RULE_CREATOR",
            "RULE_APPROVER", "RULE_VIEWER" })
    private String role;

    @Schema(description = "Token expiration time in milliseconds", example = "3600000")
    private long expiresIn; // Token expiration time in milliseconds

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    // Constructor without type (defaults to "Bearer")
    public LoginResponse(String token, Long id, String username, String email,
            String firstName, String lastName, String role, long expiresIn) {
        this.token = token;
        this.type = "Bearer";
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.expiresIn = expiresIn;
    }
}