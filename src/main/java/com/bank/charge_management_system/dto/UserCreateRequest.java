package com.bank.charge_management_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * Request DTO for creating new users
 */
@Schema(description = "Request body for creating a new user in the system")
public class UserCreateRequest {

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

    @Schema(description = "Unique username for login", example = "john_doe", required = true, minLength = 3, maxLength = 50, pattern = "^[a-zA-Z0-9_]+$")
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @Schema(description = "User's email address", example = "john.doe@example.com", required = true, maxLength = 100, format = "email")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Schema(description = "User's password (minimum 6 characters)", example = "SecurePass123", required = true, minLength = 6, maxLength = 100, format = "password")
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be 6-100 characters")
    private String password;

    @Schema(description = "User's first name", example = "John", required = true, minLength = 1, maxLength = 50)
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be 1-50 characters")
    private String firstName;

    @Schema(description = "User's last name", example = "Doe", required = true, minLength = 1, maxLength = 50)
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be 1-50 characters")
    private String lastName;

    @Schema(description = "User's role in the system", example = "RULE_VIEWER", required = true, allowableValues = {
            "ADMIN", "RULE_CREATOR", "RULE_APPROVER", "RULE_VIEWER" })
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "ADMIN|RULE_CREATOR|RULE_APPROVER|RULE_VIEWER", message = "Invalid role")
    private String role;

    @Schema(description = "Whether the user account is active", example = "true", defaultValue = "true")
    private Boolean isActive = true; // Default to active
}