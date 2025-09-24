package com.bank.charge_management_system.repository;

import com.bank.charge_management_system.entity.ChargeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChargeRuleRepository extends JpaRepository<ChargeRule, Long> {
    
    /**
     * Find rule by rule code (unique identifier)
     */
    Optional<ChargeRule> findByRuleCode(String ruleCode);
    
    /**
     * Find all active rules
     */
    List<ChargeRule> findByStatus(ChargeRule.Status status);
    
    /**
     * Find rules by category
     */
    List<ChargeRule> findByCategory(ChargeRule.Category category);
    
    /**
     * Find rules by activity type
     */
    List<ChargeRule> findByActivityType(ChargeRule.ActivityType activityType);
    
    /**
     * Find active rules by category
     */
    List<ChargeRule> findByCategoryAndStatus(ChargeRule.Category category, ChargeRule.Status status);
    
    /**
     * Find active rules for a specific transaction type
     * This will be used for charge calculation
     */
    @Query("SELECT cr FROM ChargeRule cr WHERE " +
           "cr.status = 'ACTIVE' AND " +
           "(cr.category = :category OR cr.category = 'ALL') AND " +
           "cr.effectiveFrom <= CURRENT_TIMESTAMP AND " +
           "(cr.effectiveTo IS NULL OR cr.effectiveTo > CURRENT_TIMESTAMP)")
    List<ChargeRule> findActiveRulesForCategory(@Param("category") ChargeRule.Category category);
    
    /**
     * Find rules created by a specific user
     */
    List<ChargeRule> findByCreatedBy(Long userId);
    
    /**
     * Find rules that need approval (status = DRAFT)
     */
    List<ChargeRule> findByStatusOrderByCreatedAtDesc(ChargeRule.Status status);
    
    /**
     * Search rules by name containing text (case insensitive)
     */
    @Query("SELECT cr FROM ChargeRule cr WHERE " +
           "LOWER(cr.ruleName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cr.ruleCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<ChargeRule> searchRules(@Param("searchTerm") String searchTerm);
    
    /**
     * Check if rule code already exists (for validation)
     */
    boolean existsByRuleCode(String ruleCode);
    
    /**
     * Count active rules
     */
    long countByStatus(ChargeRule.Status status);

    /**
        * Find rules with multiple optional filters
        */
       @Query("SELECT cr FROM ChargeRule cr WHERE " +
              "(:status IS NULL OR cr.status = :status) AND " +
              "(:category IS NULL OR cr.category = :category) AND " +
              "(:search IS NULL OR :search = '' OR " +
              "LOWER(cr.ruleName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
              "LOWER(cr.ruleCode) LIKE LOWER(CONCAT('%', :search, '%')))")
       List<ChargeRule> findRulesWithFilters(
       @Param("status") ChargeRule.Status status,
       @Param("category") ChargeRule.Category category, 
       @Param("search") String search
       );
}