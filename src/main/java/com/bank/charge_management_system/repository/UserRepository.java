package com.bank.charge_management_system.repository;

import com.bank.charge_management_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find all users by role
     */
    List<User> findByRole(User.Role role);
    
    /**
     * Find all active users
     */
    List<User> findByIsActive(Boolean isActive);
    
    /**
     * Find all users by role and active status
     */
    List<User> findByRoleAndIsActive(User.Role role, Boolean isActive);
    
    /**
     * Count users by role
     */
    long countByRole(User.Role role);
    
    /**
     * Count active users
     */
    long countByIsActive(Boolean isActive);
}