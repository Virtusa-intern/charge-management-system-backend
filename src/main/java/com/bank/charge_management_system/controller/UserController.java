package com.bank.charge_management_system.controller;

import com.bank.charge_management_system.dto.*;
import com.bank.charge_management_system.entity.User;
import com.bank.charge_management_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"})
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Get all users
     * GET /api/users
     * Only ADMIN can manage users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        try {
            List<UserDto> users = userService.getAllUsers();
            return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve users: " + e.getMessage(), 500));
        }
    }

    /**
     * Get user by ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        try {
            Optional<UserDto> user = userService.getUserById(id);
            
            if (user.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("User found", user.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found with id: " + id, 404));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve user: " + e.getMessage(), 500));
        }
    }

    /**
     * Get user by username
     * GET /api/users/username/{username}
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserDto>> getUserByUsername(@PathVariable String username) {
        try {
            Optional<UserDto> user = userService.getUserByUsername(username);
            
            if (user.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("User found", user.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found with username: " + username, 404));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve user: " + e.getMessage(), 500));
        }
    }

    /**
     * Create new user
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            @Valid @RequestBody UserCreateRequest request,
            BindingResult bindingResult) {
        
        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Validation failed: " + String.join(", ", errors), 400));
            }
            
            UserDto createdUser = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", createdUser));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Validation failed: " + e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create user: " + e.getMessage(), 500));
        }
    }

    /**
     * Update existing user
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            BindingResult bindingResult) {
        
        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Validation failed: " + String.join(", ", errors), 400));
            }
            
            UserDto updatedUser = userService.updateUser(id, request);
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Update failed: " + e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update user: " + e.getMessage(), 500));
        }
    }

    /**
     * Delete user
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", "User with id " + id + " has been deleted"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Delete failed: " + e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to delete user: " + e.getMessage(), 500));
        }
    }

    /**
     * Activate user
     * POST /api/users/{id}/activate
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<UserDto>> activateUser(@PathVariable Long id) {
        try {
            UserDto activatedUser = userService.activateUser(id);
            return ResponseEntity.ok(ApiResponse.success("User activated successfully", activatedUser));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Activation failed: " + e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to activate user: " + e.getMessage(), 500));
        }
    }

    /**
     * Deactivate user
     * POST /api/users/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserDto>> deactivateUser(@PathVariable Long id) {
        try {
            UserDto deactivatedUser = userService.deactivateUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", deactivatedUser));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Deactivation failed: " + e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to deactivate user: " + e.getMessage(), 500));
        }
    }

    /**
     * Get users by role
     * GET /api/users/role/{role}
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<List<UserDto>>> getUsersByRole(@PathVariable String role) {
        try {
            User.Role roleEnum = User.Role.valueOf(role.toUpperCase());
            List<UserDto> users = userService.getUsersByRole(roleEnum);
            return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully for role: " + role, users));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid role: " + role + ". Valid roles: ADMIN, RULE_CREATOR, RULE_APPROVER, RULE_VIEWER", 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve users: " + e.getMessage(), 500));
        }
    }

    /**
     * Get active users
     * GET /api/users/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<UserDto>>> getActiveUsers() {
        try {
            List<UserDto> users = userService.getActiveUsers();
            return ResponseEntity.ok(ApiResponse.success("Active users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve active users: " + e.getMessage(), 500));
        }
    }

    /**
     * Get user statistics
     * GET /api/users/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<UserService.UserStatistics>> getUserStatistics() {
        try {
            UserService.UserStatistics stats = userService.getUserStatistics();
            return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve statistics: " + e.getMessage(), 500));
        }
    }

    /**
     * Get available roles metadata
     * GET /api/users/metadata
     */
    @GetMapping("/metadata")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        
        // Available roles
        List<Map<String, String>> roles = Arrays.asList(
            createRoleInfo("ADMIN", "Administrator", "Full system access"),
            createRoleInfo("RULE_CREATOR", "Rule Creator", "Can create charge rules"),
            createRoleInfo("RULE_APPROVER", "Rule Approver", "Can approve charge rules"),
            createRoleInfo("RULE_VIEWER", "Rule Viewer", "Can view rules only")
        );
        metadata.put("roles", roles);
        
        return ResponseEntity.ok(ApiResponse.success("Metadata retrieved successfully", metadata));
    }

    /**
     * Validate username availability
     * GET /api/users/check-username/{username}
     */
    @GetMapping("/check-username/{username}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUsernameAvailability(@PathVariable String username) {
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", !userService.getUserByUsername(username).isPresent());
        return ResponseEntity.ok(ApiResponse.success("Username availability checked", result));
    }

    /**
     * Validate email availability
     * GET /api/users/check-email/{email}
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkEmailAvailability(@PathVariable String email) {
        // Simple check - in production, use proper email validation
        Map<String, Boolean> result = new HashMap<>();
        boolean isAvailable = userService.getAllUsers().stream()
            .noneMatch(user -> user.getEmail().equalsIgnoreCase(email));
        result.put("available", isAvailable);
        return ResponseEntity.ok(ApiResponse.success("Email availability checked", result));
    }

    // Helper method
    private Map<String, String> createRoleInfo(String value, String label, String description) {
        Map<String, String> roleInfo = new HashMap<>();
        roleInfo.put("value", value);
        roleInfo.put("label", label);
        roleInfo.put("description", description);
        return roleInfo;
    }
}