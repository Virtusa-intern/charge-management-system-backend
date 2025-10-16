package com.bank.charge_management_system.security;

import com.bank.charge_management_system.entity.User;
import com.bank.charge_management_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility class to get current authenticated user
 * Used for audit fields (createdBy, updatedBy)
 */
@Component
public class SecurityUtils {

    @Autowired
    private UserRepository userRepository;

    /**
     * Get current authenticated username
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            return null;
        }
        
        return authentication.getName();
    }

    /**
     * Get current authenticated user
     */
    public Optional<User> getCurrentUser() {
        String username = getCurrentUsername();
        
        if (username == null) {
            return Optional.empty();
        }
        
        return userRepository.findByUsername(username);
    }

    /**
     * Get current user ID (for audit fields)
     */
    public Long getCurrentUserId() {
        return getCurrentUser().map(User::getId).orElse(1L); // Default to admin if not authenticated
    }

    /**
     * Check if current user has a specific role
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !authentication.getName().equals("anonymousUser");
    }
}