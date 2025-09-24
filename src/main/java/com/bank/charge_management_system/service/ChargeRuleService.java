package com.bank.charge_management_system.service;

import com.bank.charge_management_system.dto.ChargeRuleCreateRequest;
import com.bank.charge_management_system.dto.ChargeRuleDto;
import com.bank.charge_management_system.dto.ChargeRuleUpdateRequest;
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
    
    /**
     * Get all charge rules
     */
    @Transactional(readOnly = true)
    public List<ChargeRuleDto> getAllRules() {
        return chargeRuleRepository.findAll()
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Get rule by ID
     */
    @Transactional(readOnly = true)
    public Optional<ChargeRuleDto> getRuleById(Long id) {
        return chargeRuleRepository.findById(id)
            .map(this::convertToDto);
    }
    
    /**
     * Get rule by rule code
     */
    @Transactional(readOnly = true)
    public Optional<ChargeRuleDto> getRuleByCode(String ruleCode) {
        return chargeRuleRepository.findByRuleCode(ruleCode)
            .map(this::convertToDto);
    }
    
    /**
     * Get rules by status
     */
    @Transactional(readOnly = true)
    public List<ChargeRuleDto> getRulesByStatus(ChargeRule.Status status) {
        return chargeRuleRepository.findByStatus(status)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Get active rules
     */
    @Transactional(readOnly = true)
    public List<ChargeRuleDto> getActiveRules() {
        return getRulesByStatus(ChargeRule.Status.ACTIVE);
    }
    
    /**
     * Get rules by category
     */
    @Transactional(readOnly = true)
    public List<ChargeRuleDto> getRulesByCategory(ChargeRule.Category category) {
        return chargeRuleRepository.findByCategory(category)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Search rules by name or code
     */
    @Transactional(readOnly = true)
    public List<ChargeRuleDto> searchRules(String searchTerm) {
        return chargeRuleRepository.searchRules(searchTerm)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Create new charge rule
     */
    public ChargeRuleDto createRule(ChargeRuleCreateRequest request) {
        // Validate rule code uniqueness
        if (chargeRuleRepository.existsByRuleCode(request.getRuleCode())) {
            throw new IllegalArgumentException("Rule code '" + request.getRuleCode() + "' already exists");
        }
        
        ChargeRule rule = new ChargeRule();
        rule.setRuleCode(request.getRuleCode());
        rule.setRuleName(request.getRuleName());
        rule.setCategory(request.getCategory());
        rule.setActivityType(request.getActivityType());
        rule.setConditions(request.getConditions());
        rule.setFeeType(request.getFeeType());
        rule.setFeeValue(request.getFeeValue());
        rule.setCurrencyCode(request.getCurrencyCode());
        rule.setMinAmount(request.getMinAmount());
        rule.setMaxAmount(request.getMaxAmount());
        rule.setThresholdCount(request.getThresholdCount());
        rule.setThresholdPeriod(request.getThresholdPeriod());
        rule.setStatus(ChargeRule.Status.DRAFT);
        rule.setEffectiveFrom(request.getEffectiveFrom() != null ? request.getEffectiveFrom() : LocalDateTime.now());
        rule.setEffectiveTo(request.getEffectiveTo());
        rule.setCreatedBy(1L); // TODO: Get from security context when authentication is implemented
        
        ChargeRule savedRule = chargeRuleRepository.save(rule);
        return convertToDto(savedRule);
    }
    
    /**
     * Update existing charge rule
     */
    public ChargeRuleDto updateRule(Long id, ChargeRuleUpdateRequest request) {
        ChargeRule rule = chargeRuleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Rule not found with id: " + id));
        
        // Only allow updates if rule is in DRAFT status
        if (rule.getStatus() != ChargeRule.Status.DRAFT) {
            throw new IllegalArgumentException("Only rules in DRAFT status can be updated");
        }
        
        rule.setRuleName(request.getRuleName());
        rule.setCategory(request.getCategory());
        rule.setActivityType(request.getActivityType());
        rule.setConditions(request.getConditions());
        rule.setFeeType(request.getFeeType());
        rule.setFeeValue(request.getFeeValue());
        rule.setCurrencyCode(request.getCurrencyCode());
        rule.setMinAmount(request.getMinAmount());
        rule.setMaxAmount(request.getMaxAmount());
        rule.setThresholdCount(request.getThresholdCount());
        rule.setThresholdPeriod(request.getThresholdPeriod());
        rule.setEffectiveFrom(request.getEffectiveFrom());
        rule.setEffectiveTo(request.getEffectiveTo());
        rule.setUpdatedBy(1L); // TODO: Get from security context
        
        ChargeRule savedRule = chargeRuleRepository.save(rule);
        return convertToDto(savedRule);
    }
    
    /**
     * Delete charge rule
     */
    public void deleteRule(Long id) {
        ChargeRule rule = chargeRuleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Rule not found with id: " + id));
        
        // Only allow deletion if rule is in DRAFT status
        if (rule.getStatus() != ChargeRule.Status.DRAFT) {
            throw new IllegalArgumentException("Only rules in DRAFT status can be deleted");
        }
        
        chargeRuleRepository.delete(rule);
    }
    
    /**
     * Approve charge rule (change status from DRAFT to ACTIVE)
     */
    public ChargeRuleDto approveRule(Long id) {
        ChargeRule rule = chargeRuleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Rule not found with id: " + id));
        
        if (rule.getStatus() != ChargeRule.Status.DRAFT) {
            throw new IllegalArgumentException("Only rules in DRAFT status can be approved");
        }
        
        rule.setStatus(ChargeRule.Status.ACTIVE);
        rule.setApprovedBy(1L); // TODO: Get from security context
        rule.setApprovedAt(LocalDateTime.now());
        
        ChargeRule savedRule = chargeRuleRepository.save(rule);
        return convertToDto(savedRule);
    }
    
    /**
     * Deactivate charge rule
     */
    public ChargeRuleDto deactivateRule(Long id) {
        ChargeRule rule = chargeRuleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Rule not found with id: " + id));
        
        if (rule.getStatus() != ChargeRule.Status.ACTIVE) {
            throw new IllegalArgumentException("Only active rules can be deactivated");
        }
        
        rule.setStatus(ChargeRule.Status.INACTIVE);
        rule.setUpdatedBy(1L); // TODO: Get from security context
        
        ChargeRule savedRule = chargeRuleRepository.save(rule);
        return convertToDto(savedRule);
    }
    
    /**
     * Get rules statistics
     */
    @Transactional(readOnly = true)
    public RuleStatistics getRuleStatistics() {
        RuleStatistics stats = new RuleStatistics();
        stats.setTotalRules(chargeRuleRepository.count());
        stats.setActiveRules(chargeRuleRepository.countByStatus(ChargeRule.Status.ACTIVE));
        stats.setDraftRules(chargeRuleRepository.countByStatus(ChargeRule.Status.DRAFT));
        stats.setInactiveRules(chargeRuleRepository.countByStatus(ChargeRule.Status.INACTIVE));
        stats.setArchivedRules(chargeRuleRepository.countByStatus(ChargeRule.Status.ARCHIVED));
        return stats;
    }
    
    /**
     * Reactivate inactive rule (INACTIVE -> ACTIVE)
     */
    public ChargeRuleDto reactivateRule(Long id) {
        ChargeRule rule = chargeRuleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Rule not found with id: " + id));
        
        if (rule.getStatus() != ChargeRule.Status.INACTIVE) {
            throw new IllegalArgumentException("Only inactive rules can be reactivated");
        }
        
        rule.setStatus(ChargeRule.Status.ACTIVE);
        rule.setUpdatedBy(1L); // TODO: Get from security context when authentication is implemented
        
        ChargeRule savedRule = chargeRuleRepository.save(rule);
        return convertToDto(savedRule);
    }
    /**
     * Convert entity to DTO
     */
    private ChargeRuleDto convertToDto(ChargeRule rule) {
        ChargeRuleDto dto = new ChargeRuleDto();
        dto.setId(rule.getId());
        dto.setRuleCode(rule.getRuleCode());
        dto.setRuleName(rule.getRuleName());
        dto.setCategory(rule.getCategory());
        dto.setActivityType(rule.getActivityType());
        dto.setConditions(rule.getConditions());
        dto.setFeeType(rule.getFeeType());
        dto.setFeeValue(rule.getFeeValue());
        dto.setCurrencyCode(rule.getCurrencyCode());
        dto.setMinAmount(rule.getMinAmount());
        dto.setMaxAmount(rule.getMaxAmount());
        dto.setThresholdCount(rule.getThresholdCount());
        dto.setThresholdPeriod(rule.getThresholdPeriod());
        dto.setStatus(rule.getStatus());
        dto.setEffectiveFrom(rule.getEffectiveFrom());
        dto.setEffectiveTo(rule.getEffectiveTo());
        dto.setCreatedAt(rule.getCreatedAt());
        dto.setUpdatedAt(rule.getUpdatedAt());
        dto.setCreatedBy(rule.getCreatedBy());
        dto.setUpdatedBy(rule.getUpdatedBy());
        dto.setApprovedBy(rule.getApprovedBy());
        dto.setApprovedAt(rule.getApprovedAt());
        
        // TODO: Add user names when user service is implemented
        dto.setCreatedByName("System User");
        dto.setUpdatedByName(rule.getUpdatedBy() != null ? "System User" : null);
        dto.setApprovedByName(rule.getApprovedBy() != null ? "System User" : null);
        
        return dto;
    }

    /**
     * Get rules with combined filters - NEW METHOD
     */
    public List<ChargeRuleDto> getRulesWithFilters(String status, String category) {
        List<ChargeRuleDto> rules;
        
        if (status != null && !status.isEmpty() && category != null && !category.isEmpty()) {
            // Both status and category filters
            ChargeRule.Status statusEnum = ChargeRule.Status.valueOf(status.toUpperCase());
            ChargeRule.Category categoryEnum = ChargeRule.Category.valueOf(category.toUpperCase());
            
            rules = chargeRuleRepository.findByCategoryAndStatus(categoryEnum, statusEnum)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
                
        } else if (status != null && !status.isEmpty()) {
            // Only status filter
            ChargeRule.Status statusEnum = ChargeRule.Status.valueOf(status.toUpperCase());
            rules = getRulesByStatus(statusEnum);
            
        } else if (category != null && !category.isEmpty()) {
            // Only category filter
            ChargeRule.Category categoryEnum = ChargeRule.Category.valueOf(category.toUpperCase());
            rules = getRulesByCategory(categoryEnum);
            
        } else {
            // No filters
            rules = getAllRules();
        }
        
        return rules;
    }
    
    /**
     * Get rules with all filters - BEST SOLUTION
     */
    public List<ChargeRuleDto> getRulesWithAllFilters(String status, String category, String search) {
        ChargeRule.Status statusEnum = null;
        ChargeRule.Category categoryEnum = null;
        
        try {
            if (status != null && !status.trim().isEmpty()) {
                statusEnum = ChargeRule.Status.valueOf(status.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        
        try {
            if (category != null && !category.trim().isEmpty()) {
                categoryEnum = ChargeRule.Category.valueOf(category.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + category);
        }
        
        String searchTerm = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        
        return chargeRuleRepository.findRulesWithFilters(statusEnum, categoryEnum, searchTerm)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
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