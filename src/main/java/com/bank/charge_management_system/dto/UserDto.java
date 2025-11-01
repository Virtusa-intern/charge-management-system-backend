package com.bank.charge_management_system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO for User responses
 * Password is never included in response for security
 */
@Schema(description = "Response containing user information. Password is never included for security.")
public class UserDto {

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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Schema(description = "Unique user identifier", example = "1")
    private Long id;

    @Schema(description = "Username for authentication", example = "john_doe")
    private String username;

    @Schema(description = "User email address", example = "john.doe@example.com", format = "email")
    private String email;

    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @Schema(description = "Full name (computed)", example = "John Doe")
    private String fullName; // Computed field: firstName + lastName

    // Role as string for frontend
    @Schema(description = "User role", example = "RULE_VIEWER", allowableValues = { "ADMIN", "RULE_CREATOR",
            "RULE_APPROVER", "RULE_VIEWER" })
    private String role;

    @Schema(description = "Whether user account is active", example = "true")
    private Boolean isActive;

    // Audit fields
    @Schema(description = "Account creation timestamp", example = "2024-01-01T10:00:00", format = "date-time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2024-01-15T14:30:00", format = "date-time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "User ID who created this account", example = "1")
    private Long createdBy;

    @Schema(description = "User ID who last updated this account", example = "2")
    private Long updatedBy;

    // Note: Password is NEVER included in DTO for security reasons
}