package com.bank.charge_management_system.service;

import com.bank.charge_management_system.dto.*;
import com.bank.charge_management_system.entity.ChargeRule;
import com.bank.charge_management_system.repository.ChargeRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChargeRuleService {

    @Autowired
    private ChargeRuleRepository chargeRuleRepository;

    // ========== READ OPERATIONS ==========

    /**
     * Get all rules with combined filtering (status, category, search)
     */
    public List<ChargeRuleDto> getRulesWithAllFilters(String statusStr, String categoryStr, String search) {
        ChargeRule.Status status = null;
        ChargeRule.Category category = null;
        
        // Parse status if provided
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = ChargeRule.Status.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + statusStr);
            }
        }
        
        // Parse category if provided
        if (categoryStr != null && !categoryStr.isEmpty()) {
            try {
                category = ChargeRule.Category.valueOf(categoryStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid category: " + categoryStr);
            }
        }
        
        // Use repository method with filters
        List<ChargeRule> rules = chargeRuleRepository.findRulesWithFilters(status, category, search);
        
        return rules.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * Get rule by ID
     */
    public Optional<ChargeRuleDto> getRuleById(Long id) {
        return chargeRuleRepository.findById(id)
            .map(this::convertToDto);
    }

    /**
     * Get rule by code
     */
    public Optional<ChargeRuleDto> getRuleByCode(String ruleCode) {
        return chargeRuleRepository.findByRuleCode(ruleCode)
            .map(this::convertToDto);
    }

    /**
     * Get all rules by status
     */
    public List<ChargeRuleDto> getRulesByStatus(ChargeRule.Status status) {
        return chargeRuleRepository.findByStatus(status).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * Get all rules by category
     */
    public List<ChargeRuleDto> getRulesByCategory(ChargeRule.Category category) {
        return chargeRuleRepository.findByCategory(category).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * Get all active rules
     */
    public List<ChargeRuleDto> getActiveRules() {
        return getRulesByStatus(ChargeRule.Status.ACTIVE);
    }

    /**
     * Search rules by name or code
     */
    public List<ChargeRuleDto> searchRules(String searchTerm) {
        return chargeRuleRepository.searchRules(searchTerm).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    // ========== CREATE OPERATION ==========

    /**
     * Create new charge rule
     */
    public ChargeRuleDto createRule(ChargeRuleCreateRequest request) {
        // Validate rule code uniqueness
        if (chargeRuleRepository.existsByRuleCode(request.getRuleCode())) {
            throw new IllegalArgumentException("Rule code already exists: " + request.getRuleCode());
        }
        
        // Validate business rules
        validateRuleRequest(request);
        
        // Create new rule entity
        ChargeRule rule = new ChargeRule();
        rule.setRuleCode(request.getRuleCode());
        rule.setRuleName(request.getRuleName());
        
        // Convert String to Enum - these are already Strings from the DTO
        rule.setCategory(request.getCategory());
        rule.setActivityType(request.getActivityType());
        rule.setFeeType(request.getFeeType());
        
        rule.setConditions(request.getConditions());
        rule.setFeeValue(request.getFeeValue());
        rule.setCurrencyCode(request.getCurrencyCode() != null ? request.getCurrencyCode() : "INR");
        rule.setMinAmount(request.getMinAmount());
        rule.setMaxAmount(request.getMaxAmount());
        rule.setThresholdCount(request.getThresholdCount() != null ? request.getThresholdCount() : 0);
        
        if (request.getThresholdPeriod() != null) {
            rule.setThresholdPeriod(request.getThresholdPeriod());
        }
        
        // Set status - new rules start as DRAFT
        rule.setStatus(ChargeRule.Status.DRAFT);
        rule.setEffectiveFrom(request.getEffectiveFrom() != null ? request.getEffectiveFrom() : LocalDateTime.now());
        rule.setEffectiveTo(request.getEffectiveTo());
        
        // Set audit fields (TODO: Get from security context when auth is implemented)
        rule.setCreatedBy(1L); // Default to admin user
        
        // Save rule
        ChargeRule savedRule = chargeRuleRepository.save(rule);
        
        return convertToDto(savedRule);
    }

    // ========== UPDATE OPERATION ==========

    /**
     * Update existing charge rule
     */
    public ChargeRuleDto updateRule(Long id, ChargeRuleUpdateRequest request) {
        ChargeRule rule = chargeRuleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Rule not found with id: " + id));
        
        // Only DRAFT and INACTIVE rules can be fully updated
        if (rule.getStatus() == ChargeRule.Status.ACTIVE) {
            throw new IllegalArgumentException("Cannot update ACTIVE rule. Please deactivate it first.");
        }
        
        if (rule.getStatus() == ChargeRule.Status.ARCHIVED) {
            throw new IllegalArgumentException("Cannot update ARCHIVED rule.");
        }
        
        // Validate if rule code is being changed and if it's unique
        if (request.getRuleCode() != null && !request.getRuleCode().isEmpty() && 
            !request.getRuleCode().equals(rule.getRuleCode())) {
            if (chargeRuleRepository.existsByRuleCode(request.getRuleCode())) {
                throw new IllegalArgumentException("Rule code already exists: " + request.getRuleCode());
            }
            rule.setRuleCode(request.getRuleCode());
        }
        
        // Update fields if provided (request fields are Strings)
        if (request.getRuleName() != null && !request.getRuleName().isEmpty()) {
            rule.setRuleName(request.getRuleName());
        }
        
        if (request.getCategory() != null) {
            rule.setCategory(request.getCategory());
        }
        
        if (request.getActivityType() != null) {
            rule.setActivityType(request.getActivityType());
        }
        
        if (request.getConditions() != null) {
            rule.setConditions(request.getConditions());
        }
        
        if (request.getFeeType() != null) {
            rule.setFeeType(request.getFeeType());
        }
        
        if (request.getFeeValue() != null) {
            rule.setFeeValue(request.getFeeValue());
        }
        
        if (request.getMinAmount() != null) {
            rule.setMinAmount(request.getMinAmount());
        }
        
        if (request.getMaxAmount() != null) {
            rule.setMaxAmount(request.getMaxAmount());
        }
        
        if (request.getThresholdCount() != null) {
            rule.setThresholdCount(request.getThresholdCount());
        }
        
        if (request.getThresholdPeriod() != null) {
            rule.setThresholdPeriod(request.getThresholdPeriod());
        }
        
        if (request.getEffectiveFrom() != null) {
            rule.setEffectiveFrom(request.getEffectiveFrom());
        }
        
        if (request.getEffectiveTo() != null) {
            rule.setEffectiveTo(request.getEffectiveTo());
        }
        
        // Update audit fields
        rule.setUpdatedBy(1L); // TODO: Get from security context
        
        // Validate updated rule
        validateRule(rule);
        
        ChargeRule updatedRule = chargeRuleRepository.save(rule);
        return convertToDto(updatedRule);
    }

    // ========== DELETE OPERATION ==========

    /**
     * Delete charge rule
     * Only DRAFT and INACTIVE rules can be deleted
     */
    public void deleteRule(Long id) {
        ChargeRule rule = chargeRuleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Rule not found with id: " + id));
        
        // Business rule: Can only delete DRAFT or INACTIVE rules
        if (rule.getStatus() == ChargeRule.Status.ACTIVE) {
            throw new IllegalArgumentException("Cannot delete ACTIVE rule. Please deactivate it first.");
        }
        
        if (rule.getStatus() == ChargeRule.Status.ARCHIVED) {
            throw new IllegalArgumentException("Cannot delete ARCHIVED rule.");
        }
        
        chargeRuleRepository.delete(rule);
    }

    // ========== WORKFLOW OPERATIONS ==========

    /**
     * Approve rule (DRAFT -> ACTIVE)
     */
    public ChargeRuleDto approveRule(Long id) {
        ChargeRule rule = chargeRuleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Rule not found with id: " + id));
        
        if (rule.getStatus() != ChargeRule.Status.DRAFT) {
            throw new IllegalArgumentException("Only DRAFT rules can be approved. Current status: " + rule.getStatus());
        }
        
        rule.setStatus(ChargeRule.Status.ACTIVE);
        rule.setApprovedBy(1L); // TODO: Get from security context
        rule.setApprovedAt(LocalDateTime.now());
        
        ChargeRule approvedRule = chargeRuleRepository.save(rule);
        return convertToDto(approvedRule);
    }

    /**
     * Deactivate rule (ACTIVE -> INACTIVE)
     */
    public ChargeRuleDto deactivateRule(Long id) {
        ChargeRule rule = chargeRuleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Rule not found with id: " + id));
        
        if (rule.getStatus() != ChargeRule.Status.ACTIVE) {
            throw new IllegalArgumentException("Only ACTIVE rules can be deactivated. Current status: " + rule.getStatus());
        }
        
        rule.setStatus(ChargeRule.Status.INACTIVE);
        rule.setUpdatedBy(1L); // TODO: Get from security context
        rule.setEffectiveTo(LocalDateTime.now());
        
        ChargeRule deactivatedRule = chargeRuleRepository.save(rule);
        return convertToDto(deactivatedRule);
    }

    /**
     * Reactivate rule (INACTIVE -> ACTIVE)
     */
    public ChargeRuleDto reactivateRule(Long id) {
        ChargeRule rule = chargeRuleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Rule not found with id: " + id));
        
        if (rule.getStatus() != ChargeRule.Status.INACTIVE) {
            throw new IllegalArgumentException("Only INACTIVE rules can be reactivated. Current status: " + rule.getStatus());
        }
        
        rule.setStatus(ChargeRule.Status.ACTIVE);
        rule.setUpdatedBy(1L); // TODO: Get from security context
        rule.setEffectiveTo(null); // Remove end date
        
        ChargeRule reactivatedRule = chargeRuleRepository.save(rule);
        return convertToDto(reactivatedRule);
    }

    // ========== STATISTICS ==========

    /**
     * Get rule statistics
     */
    public RuleStatistics getRuleStatistics() {
        RuleStatistics stats = new RuleStatistics();
        
        stats.setTotalRules(chargeRuleRepository.count());
        stats.setActiveRules(chargeRuleRepository.countByStatus(ChargeRule.Status.ACTIVE));
        stats.setDraftRules(chargeRuleRepository.countByStatus(ChargeRule.Status.DRAFT));
        stats.setInactiveRules(chargeRuleRepository.countByStatus(ChargeRule.Status.INACTIVE));
        stats.setArchivedRules(chargeRuleRepository.countByStatus(ChargeRule.Status.ARCHIVED));
        
        return stats;
    }

    // ========== VALIDATION ==========

    /**
     * Validate rule request
     */
    private void validateRuleRequest(ChargeRuleCreateRequest request) {
        if (request.getFeeValue().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Fee value cannot be negative");
        }
        
        if (request.getMaxAmount() != null && request.getMinAmount() != null) {
            if (request.getMaxAmount().compareTo(request.getMinAmount()) <= 0) {
                throw new IllegalArgumentException("Maximum amount must be greater than minimum amount");
            }
        }
        
        if (request.getRuleCode() != null && !request.getRuleCode().matches("^[0-9A-Z]+$")) {
            throw new IllegalArgumentException("Rule code must contain only numbers and uppercase letters");
        }
    }

    /**
     * Validate rule entity
     */
    private void validateRule(ChargeRule rule) {
        if (rule.getFeeValue().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Fee value cannot be negative");
        }
        
        if (rule.getMaxAmount() != null && rule.getMinAmount() != null) {
            if (rule.getMaxAmount().compareTo(rule.getMinAmount()) <= 0) {
                throw new IllegalArgumentException("Maximum amount must be greater than minimum amount");
            }
        }
    }

    // ========== DTO CONVERSION ==========

    /**
     * Convert ChargeRule entity to DTO
     */
    private ChargeRuleDto convertToDto(ChargeRule rule) {
        ChargeRuleDto dto = new ChargeRuleDto();
        
        dto.setId(rule.getId());
        dto.setRuleCode(rule.getRuleCode());
        dto.setRuleName(rule.getRuleName());
        dto.setCategory(rule.getCategory().toString());
        dto.setActivityType(rule.getActivityType().toString());
        dto.setConditions(rule.getConditions());
        dto.setFeeType(rule.getFeeType().toString());
        dto.setFeeValue(rule.getFeeValue());
        dto.setCurrencyCode(rule.getCurrencyCode());
        dto.setMinAmount(rule.getMinAmount());
        dto.setMaxAmount(rule.getMaxAmount());
        dto.setThresholdCount(rule.getThresholdCount());
        dto.setThresholdPeriod(rule.getThresholdPeriod() != null ? rule.getThresholdPeriod().toString() : null);
        dto.setStatus(rule.getStatus().toString());
        dto.setEffectiveFrom(rule.getEffectiveFrom());
        dto.setEffectiveTo(rule.getEffectiveTo());
        dto.setCreatedAt(rule.getCreatedAt());
        dto.setUpdatedAt(rule.getUpdatedAt());
        dto.setCreatedBy(rule.getCreatedBy());
        dto.setUpdatedBy(rule.getUpdatedBy());
        dto.setApprovedBy(rule.getApprovedBy());
        dto.setApprovedAt(rule.getApprovedAt());
        
        return dto;
    }

    // ========== INNER CLASSES ==========

    /**
     * Statistics DTO
     */
    public static class RuleStatistics {
        private long totalRules;
        private long activeRules;
        private long draftRules;
        private long inactiveRules;
        private long archivedRules;
        
        // Getters and setters
        public long getTotalRules() { return totalRules; }
        public void setTotalRules(long totalRules) { this.totalRules = totalRules; }
        
        public long getActiveRules() { return activeRules; }
        public void setActiveRules(long activeRules) { this.activeRules = activeRules; }
        
        public long getDraftRules() { return draftRules; }
        public void setDraftRules(long draftRules) { this.draftRules = draftRules; }
        
        public long getInactiveRules() { return inactiveRules; }
        public void setInactiveRules(long inactiveRules) { this.inactiveRules = inactiveRules; }
        
        public long getArchivedRules() { return archivedRules; }
        public void setArchivedRules(long archivedRules) { this.archivedRules = archivedRules; }
    }
}