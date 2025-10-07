// src/main/java/com/bank/charge_management_system/repository/UserRepository.java
package com.bank.charge_management_system.repository;

import com.bank.charge_management_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * Find users by role
     */
    List<User> findByRole(User.Role role);
    
    /**
     * Find active users
     */
    List<User> findByIsActive(Boolean isActive);
    
    /**
     * Find users by role and active status
     */
    List<User> findByRoleAndIsActive(User.Role role, Boolean isActive);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Search users by name or username
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);
    
    /**
     * Count users by role
     */
    long countByRole(User.Role role);
    
    /**
     * Count active users
     */
    long countByIsActive(Boolean isActive);
    
    /**
     * Find all active users ordered by creation date
     */
    List<User> findByIsActiveTrueOrderByCreatedAtDesc();
    
    /**
     * Get user statistics
     */
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> getUserStatisticsByRole();
}