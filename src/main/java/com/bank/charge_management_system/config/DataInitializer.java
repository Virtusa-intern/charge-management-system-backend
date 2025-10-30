package com.bank.charge_management_system.config;

import com.bank.charge_management_system.entity.User;
import com.bank.charge_management_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data Initializer - Runs on application startup
 * Creates default users with properly hashed passwords
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 DataInitializer: Checking default users...");
        
        // Create default admin if not exists
        if (!userRepository.existsByUsername("admin")) {
            createDefaultUser(
                "admin", 
                "admin@chargemgmt.com", 
                "admin123",  // Plain password - will be hashed
                "System", 
                "Administrator", 
                User.Role.ADMIN
            );
            System.out.println("✅ Created default ADMIN user (username: admin, password: admin123)");
        }

        // Create default rule creator if not exists
        if (!userRepository.existsByUsername("creator")) {
            createDefaultUser(
                "creator", 
                "creator@chargemgmt.com", 
                "creator123",
                "Rule", 
                "Creator", 
                User.Role.RULE_CREATOR
            );
            System.out.println("✅ Created default RULE_CREATOR user (username: creator, password: creator123)");
        }

        // Create default rule approver if not exists
        if (!userRepository.existsByUsername("approver")) {
            createDefaultUser(
                "approver", 
                "approver@chargemgmt.com", 
                "approver123",
                "Rule", 
                "Approver", 
                User.Role.RULE_APPROVER
            );
            System.out.println("✅ Created default RULE_APPROVER user (username: approver, password: approver123)");
        }

        // Create default rule viewer if not exists
        if (!userRepository.existsByUsername("viewer")) {
            createDefaultUser(
                "viewer", 
                "viewer@chargemgmt.com", 
                "viewer123",
                "Rule", 
                "Viewer", 
                User.Role.RULE_VIEWER
            );
            System.out.println("✅ Created default RULE_VIEWER user (username: viewer, password: viewer123)");
        }

        System.out.println("🎉 DataInitializer: Initialization complete!");
    }

    /**
     * Helper method to create a user with hashed password
     */
    private void createDefaultUser(String username, String email, String plainPassword, 
                                   String firstName, String lastName, User.Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        
        // ⭐ MAGIC HAPPENS HERE: BCrypt password hashing
        user.setPasswordHash(passwordEncoder.encode(plainPassword));
        
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setIsActive(true);
        user.setCreatedBy(1L); // System user
        
        userRepository.save(user);
    }
}