package com.bank.charge_management_system.service;

import com.bank.charge_management_system.dto.*;
import com.bank.charge_management_system.entity.User;
import com.bank.charge_management_system.repository.UserRepository;
import com.bank.charge_management_system.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityUtils securityUtils;

    // ========== READ OPERATIONS ==========

    /**
     * Get all users
     */
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     */
    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id)
            .map(this::convertToDto);
    }

    /**
     * Get user by username
     */
    public Optional<UserDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(this::convertToDto);
    }

    /**
     * Get users by role
     */
    public List<UserDto> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * Get active users
     */
    public List<UserDto> getActiveUsers() {
        return userRepository.findByIsActive(true).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    // ========== CREATE OPERATION ==========

    /**
     * Create new user
     */
    public UserDto createUser(UserCreateRequest request) {
        // Validate username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        // Validate business rules
        validateUserRequest(request);

        // Create new user entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        
        // Use BCrypt to hash password
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(User.Role.valueOf(request.getRole().toUpperCase()));
        user.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        // Set audit fields using SecurityUtils
        user.setCreatedBy(securityUtils.getCurrentUserId());

        // Save user
        User savedUser = userRepository.save(user);

        return convertToDto(savedUser);
    }

    // ========== UPDATE OPERATION ==========

    /**
     * Update existing user
     */
    public UserDto updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Validate if username is being changed and if it's unique
        if (request.getUsername() != null && !request.getUsername().isEmpty() && 
            !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username already exists: " + request.getUsername());
            }
            user.setUsername(request.getUsername());
        }

        // Validate if email is being changed and if it's unique
        if (request.getEmail() != null && !request.getEmail().isEmpty() && 
            !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // Update fields if provided
        if (request.getFirstName() != null && !request.getFirstName().isEmpty()) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null && !request.getLastName().isEmpty()) {
            user.setLastName(request.getLastName());
        }

        if (request.getRole() != null && !request.getRole().isEmpty()) {
            user.setRole(User.Role.valueOf(request.getRole().toUpperCase()));
        }

        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            // Hash password with BCrypt
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        // Update audit fields using SecurityUtils
        user.setUpdatedBy(securityUtils.getCurrentUserId());

        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    // ========== DELETE OPERATION ==========

    /**
     * Delete user
     * Only inactive users or non-admin users can be deleted
     */
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Business rule: Cannot delete admin user with id 1 (system admin)
        if (user.getId() == 1L) {
            throw new IllegalArgumentException("Cannot delete system administrator");
        }

        // Business rule: Should deactivate active users first
        if (user.getIsActive()) {
            throw new IllegalArgumentException("Cannot delete active user. Please deactivate first.");
        }

        userRepository.delete(user);
    }

    // ========== STATUS OPERATIONS ==========

    /**
     * Activate user
     */
    public UserDto activateUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setIsActive(true);
        user.setUpdatedBy(securityUtils.getCurrentUserId());

        User activatedUser = userRepository.save(user);
        return convertToDto(activatedUser);
    }

    /**
     * Deactivate user
     */
    public UserDto deactivateUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Business rule: Cannot deactivate system admin
        if (user.getId() == 1L) {
            throw new IllegalArgumentException("Cannot deactivate system administrator");
        }

        user.setIsActive(false);
        user.setUpdatedBy(securityUtils.getCurrentUserId());

        User deactivatedUser = userRepository.save(user);
        return convertToDto(deactivatedUser);
    }

    // ========== STATISTICS ==========

    /**
     * Get user statistics
     */
    public UserStatistics getUserStatistics() {
        UserStatistics stats = new UserStatistics();

        stats.setTotalUsers(userRepository.count());
        stats.setActiveUsers(userRepository.countByIsActive(true));
        stats.setInactiveUsers(userRepository.countByIsActive(false));
        stats.setAdminUsers(userRepository.countByRole(User.Role.ADMIN));
        stats.setCreatorUsers(userRepository.countByRole(User.Role.RULE_CREATOR));
        stats.setApproverUsers(userRepository.countByRole(User.Role.RULE_APPROVER));
        stats.setViewerUsers(userRepository.countByRole(User.Role.RULE_VIEWER));

        return stats;
    }

    // ========== VALIDATION ==========

    /**
     * Validate user request
     */
    private void validateUserRequest(UserCreateRequest request) {
        // Validate username format
        if (!request.getUsername().matches("^[a-zA-Z0-9_]{3,50}$")) {
            throw new IllegalArgumentException("Username must be 3-50 characters and contain only letters, numbers, and underscores");
        }

        // Validate email format
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Validate password strength (basic)
        if (request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        // Validate names
        if (request.getFirstName().trim().isEmpty() || request.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name and last name are required");
        }
    }

    // ========== DTO CONVERSION ==========

    /**
     * Convert User entity to DTO
     */
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();

        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole().toString());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setCreatedBy(user.getCreatedBy());
        dto.setUpdatedBy(user.getUpdatedBy());

        // Build full name
        dto.setFullName(user.getFirstName() + " " + user.getLastName());

        return dto;
    }

    // ========== INNER CLASSES ==========

    /**
     * Statistics DTO
     */
    public static class UserStatistics {
        private long totalUsers;
        private long activeUsers;
        private long inactiveUsers;
        private long adminUsers;
        private long creatorUsers;
        private long approverUsers;
        private long viewerUsers;

        // Getters and setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

        public long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

        public long getInactiveUsers() { return inactiveUsers; }
        public void setInactiveUsers(long inactiveUsers) { this.inactiveUsers = inactiveUsers; }

        public long getAdminUsers() { return adminUsers; }
        public void setAdminUsers(long adminUsers) { this.adminUsers = adminUsers; }

        public long getCreatorUsers() { return creatorUsers; }
        public void setCreatorUsers(long creatorUsers) { this.creatorUsers = creatorUsers; }

        public long getApproverUsers() { return approverUsers; }
        public void setApproverUsers(long approverUsers) { this.approverUsers = approverUsers; }

        public long getViewerUsers() { return viewerUsers; }
        public void setViewerUsers(long viewerUsers) { this.viewerUsers = viewerUsers; }
    }
}