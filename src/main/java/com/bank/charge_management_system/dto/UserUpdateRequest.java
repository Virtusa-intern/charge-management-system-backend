package com.bank.charge_management_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * Request DTO for updating users
 * All fields are optional - only provided fields will be updated
 */
@Schema(description = "Request body for updating an existing user. All fields are optional - only provided fields will be updated.")
public class UserUpdateRequest {

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Schema(description = "Username (optional, only if changing)", example = "john_doe_updated", minLength = 3, maxLength = 50, pattern = "^[a-zA-Z0-9_]+$")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @Schema(description = "Email address (optional, only if changing)", example = "john.updated@example.com", maxLength = 100, format = "email")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Schema(description = "New password (optional, only if changing)", example = "NewSecurePass456", minLength = 6, maxLength = 100, format = "password")
    @Size(min = 6, max = 100, message = "Password must be 6-100 characters")
    private String password; // Optional - only update if provided

    @Schema(description = "First name (optional, only if changing)", example = "Jonathan", minLength = 1, maxLength = 50)
    @Size(min = 1, max = 50, message = "First name must be 1-50 characters")
    private String firstName;

    @Schema(description = "Last name (optional, only if changing)", example = "Doe-Smith", minLength = 1, maxLength = 50)
    @Size(min = 1, max = 50, message = "Last name must be 1-50 characters")
    private String lastName;

    @Schema(description = "User role (optional, only if changing)", example = "RULE_APPROVER", allowableValues = {
            "ADMIN", "RULE_CREATOR", "RULE_APPROVER", "RULE_VIEWER" })
    @Pattern(regexp = "ADMIN|RULE_CREATOR|RULE_APPROVER|RULE_VIEWER", message = "Invalid role")
    private String role;

    @Schema(description = "Account active status (optional, only if changing)", example = "false")
    private Boolean isActive;
}