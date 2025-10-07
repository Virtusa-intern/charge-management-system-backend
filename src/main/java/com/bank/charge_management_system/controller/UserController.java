package com.bank.charge_management_system.controller;

import com.bank.charge_management_system.dto.ApiResponse;
import com.bank.charge_management_system.entity.User;
import com.bank.charge_management_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"})
public class UserController {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get all users
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
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
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
            
            return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage(), 404));
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
    public ResponseEntity<ApiResponse<User>> getUserByUsername(@PathVariable String username) {
        try {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
            
            return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage(), 404));
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
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
        try {
            // Validate username uniqueness
            if (userRepository.existsByUsername(user.getUsername())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Username already exists: " + user.getUsername(), 400));
            }
            
            // Validate email uniqueness
            if (userRepository.existsByEmail(user.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email already exists: " + user.getEmail(), 400));
            }
            
            // Set default values if not provided
            if (user.getRole() == null) {
                user.setRole(User.Role.RULE_VIEWER);
            }
            
            if (user.getIsActive() == null) {
                user.setIsActive(true);
            }
            
            // For now, set a default password hash (in production, use proper password hashing)
            if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
                user.setPasswordHash("defaultPassword"); // TODO: Implement proper password hashing
            }
            
            User savedUser = userRepository.save(user);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", savedUser));
                
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
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable Long id, 
            @RequestBody User updatedUser) {
        try {
            User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
            
            // Check if username is being changed and if it's already taken
            if (!existingUser.getUsername().equals(updatedUser.getUsername())) {
                if (userRepository.existsByUsername(updatedUser.getUsername())) {
                    return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Username already exists: " + updatedUser.getUsername(), 400));
                }
            }
            
            // Check if email is being changed and if it's already taken
            if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
                if (userRepository.existsByEmail(updatedUser.getEmail())) {
                    return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email already exists: " + updatedUser.getEmail(), 400));
                }
            }
            
            // Update fields
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setFirstName(updatedUser.getFirstName());
            existingUser.setLastName(updatedUser.getLastName());
            existingUser.setRole(updatedUser.getRole());
            existingUser.setIsActive(updatedUser.getIsActive());
            
            User savedUser = userRepository.save(existingUser);
            
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", savedUser));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage(), 404));
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
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            if (!userRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found with id: " + id, 404));
            }
            
            userRepository.deleteById(id);
            
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to delete user: " + e.getMessage(), 500));
        }
    }
    
    /**
     * Get users by role
     * GET /api/users/role/{role}
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByRole(@PathVariable String role) {
        try {
            User.Role userRole = User.Role.valueOf(role.toUpperCase());
            List<User> users = userRepository.findByRole(userRole);
            
            return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid role: " + role, 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve users: " + e.getMessage(), 500));
        }
    }
    
    /**
     * Get active users only
     * GET /api/users/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<User>>> getActiveUsers() {
        try {
            List<User> users = userRepository.findByIsActive(true);
            return ResponseEntity.ok(ApiResponse.success("Active users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve active users: " + e.getMessage(), 500));
        }
    }
    
    /**
     * Search users
     * GET /api/users/search?q={searchTerm}
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<User>>> searchUsers(@RequestParam String q) {
        try {
            List<User> users = userRepository.searchUsers(q);
            return ResponseEntity.ok(ApiResponse.success("Search completed successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Search failed: " + e.getMessage(), 500));
        }
    }
    
    /**
     * Get user statistics
     * GET /api/users/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            statistics.put("totalUsers", userRepository.count());
            statistics.put("activeUsers", userRepository.countByIsActive(true));
            statistics.put("inactiveUsers", userRepository.countByIsActive(false));
            
            // Count by role
            Map<String, Long> roleCount = new HashMap<>();
            for (User.Role role : User.Role.values()) {
                roleCount.put(role.name(), userRepository.countByRole(role));
            }
            statistics.put("usersByRole", roleCount);
            
            return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", statistics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve statistics: " + e.getMessage(), 500));
        }
    }
    
    /**
     * Toggle user active status
     * PATCH /api/users/{id}/toggle-status
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<User>> toggleUserStatus(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
            
            user.setIsActive(!user.getIsActive());
            User savedUser = userRepository.save(user);
            
            String message = savedUser.getIsActive() ? "User activated successfully" : "User deactivated successfully";
            return ResponseEntity.ok(ApiResponse.success(message, savedUser));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage(), 404));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to toggle user status: " + e.getMessage(), 500));
        }
    }
}