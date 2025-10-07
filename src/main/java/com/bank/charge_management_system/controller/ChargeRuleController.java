// src/main/java/com/bank/chargemgmt/controller/ChargeRuleController.java
package com.bank.charge_management_system.controller;

import com.bank.charge_management_system.dto.*;
import com.bank.charge_management_system.entity.ChargeRule;
import com.bank.charge_management_system.service.ChargeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;

@RestController
@RequestMapping("/api/rules")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"})
public class ChargeRuleController {

    @Autowired
    private ChargeRuleService chargeRuleService;

    /**
     * Get all charge rules with combined filtering - FINAL VERSION  
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChargeRuleDto>>> getAllRules(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        
        try {
            List<ChargeRuleDto> rules = chargeRuleService.getRulesWithAllFilters(status, category, search);
            return ResponseEntity.ok(ApiResponse.success("Rules retrieved successfully", rules));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid parameter: " + e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve rules: " + e.getMessage(), 500));
        }
    }
        /**
         * Get rule by ID
     * GET /api/rules/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChargeRuleDto>> getRuleById(@PathVariable Long id) {
        try {
            Optional<ChargeRuleDto> rule = chargeRuleService.getRuleById(id);
            
            if (rule.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Rule found", rule.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Rule not found with id: " + id, 404));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve rule: " + e.getMessage(), 500));
        }
    }

    /**
     * Get rule by rule code
     * GET /api/rules/code/{ruleCode}
     */
    @GetMapping("/code/{ruleCode}")
    public ResponseEntity<ApiResponse<ChargeRuleDto>> getRuleByCode(@PathVariable String ruleCode) {
        try {
            Optional<ChargeRuleDto> rule = chargeRuleService.getRuleByCode(ruleCode);
            
            if (rule.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Rule found", rule.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Rule not found with code: " + ruleCode, 404));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve rule: " + e.getMessage(), 500));
        }
    }

    /**
     * Create new charge rule
     * POST /api/rules
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ChargeRuleDto>> createRule(
            @Valid @RequestBody ChargeRuleCreateRequest request,
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
            
            ChargeRuleDto createdRule = chargeRuleService.createRule(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rule created successfully", createdRule));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Validation failed: " + e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create rule: " + e.getMessage(), 500));
        }
    }

    /**
     * Update existing charge rule
     * PUT /api/rules/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ChargeRuleDto>> updateRule(
            @PathVariable Long id, 
            @Valid @RequestBody ChargeRuleUpdateRequest request,
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
            
            ChargeRuleDto updatedRule = chargeRuleService.updateRule(id, request);
            return ResponseEntity.ok(ApiResponse.success("Rule updated successfully", updatedRule));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Update failed: " + e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update rule: " + e.getMessage(), 500));
        }
    }

    /**
     * Delete charge rule
     * DELETE /api/rules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRule(@PathVariable Long id) {
        try {
            chargeRuleService.deleteRule(id);
            return ResponseEntity.ok(ApiResponse.success("Rule deleted successfully", "Rule with id " + id + " has been deleted"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Delete failed: " + e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to delete rule: " + e.getMessage(), 500));
        }
    }

    /**
     * Approve charge rule (DRAFT -> ACTIVE)
     * POST /api/rules/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ChargeRuleDto>> approveRule(@PathVariable Long id) {
        try {
            ChargeRuleDto approvedRule = chargeRuleService.approveRule(id);
            return ResponseEntity.ok(ApiResponse.success("Rule approved successfully", approvedRule));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Approval failed: " + e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to approve rule: " + e.getMessage(), 500));
        }
    }

    /**
     * Deactivate charge rule (ACTIVE -> INACTIVE)
     * POST /api/rules/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<ChargeRuleDto>> deactivateRule(@PathVariable Long id) {
        try {
            ChargeRuleDto deactivatedRule = chargeRuleService.deactivateRule(id);
            return ResponseEntity.ok(ApiResponse.success("Rule deactivated successfully", deactivatedRule));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Deactivation failed: " + e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to deactivate rule: " + e.getMessage(), 500));
        }
    }

    /**
     * Reactivate inactive rule (INACTIVE -> ACTIVE)
     * POST /api/rules/{id}/reactivate
     */
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponse<ChargeRuleDto>> reactivateRule(@PathVariable Long id) {
        try {
            ChargeRuleDto reactivatedRule = chargeRuleService.reactivateRule(id);
            return ResponseEntity.ok(ApiResponse.success("Rule reactivated successfully", reactivatedRule));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Reactivation failed: " + e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to reactivate rule: " + e.getMessage(), 500));
        }
    }

        /**
         * Get draft rules that need approval
         * GET /api/rules/pending-approval
     */
    @GetMapping("/pending-approval")
    public ResponseEntity<ApiResponse<List<ChargeRuleDto>>> getPendingApprovalRules() {
        try {
            List<ChargeRuleDto> draftRules = chargeRuleService.getRulesByStatus(ChargeRule.Status.DRAFT);
            return ResponseEntity.ok(ApiResponse.success("Pending approval rules retrieved successfully", draftRules));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve pending approval rules: " + e.getMessage(), 500));
        }
    }

    /**
     * Get rules statistics
     * GET /api/rules/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<ChargeRuleService.RuleStatistics>> getRuleStatistics() {
        try {
            ChargeRuleService.RuleStatistics stats = chargeRuleService.getRuleStatistics();
            return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", stats));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve statistics: " + e.getMessage(), 500));
        }
    }

    /**
     * Get rules by category
     * GET /api/rules/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ChargeRuleDto>>> getRulesByCategory(@PathVariable String category) {
        try {
            ChargeRule.Category categoryEnum = ChargeRule.Category.valueOf(category.toUpperCase());
            List<ChargeRuleDto> rules = chargeRuleService.getRulesByCategory(categoryEnum);
            return ResponseEntity.ok(ApiResponse.success("Rules retrieved successfully for category: " + category, rules));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid category: " + category + ". Valid categories: RETAIL_BANKING, CORP_BANKING, ALL", 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve rules: " + e.getMessage(), 500));
        }
    }

    /**
     * Get available enum values for dropdowns
     * GET /api/rules/metadata
     */
        @GetMapping("/metadata")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getRuleMetadata() {
            Map<String, Object> metadata = new HashMap<>();
            
            // Available statuses
            List<String> statuses = Arrays.asList("ACTIVE", "DRAFT", "INACTIVE", "ARCHIVED");
            metadata.put("statuses", statuses);
            
            // Available categories
            List<String> categories = Arrays.asList("RETAIL_BANKING", "CORP_BANKING", "ALL");
            metadata.put("categories", categories);
            
            // Available activity types
            List<String> activityTypes = Arrays.asList("UNIT_WISE", "RANGE_BASED", "MONTHLY", "SPECIAL", "ADHOC");
            metadata.put("activityTypes", activityTypes);
            
            // Available fee types
            List<String> feeTypes = Arrays.asList("PERCENTAGE", "FLAT_AMOUNT", "TIERED");
            metadata.put("feeTypes", feeTypes);
            
            return ResponseEntity.ok(ApiResponse.success("Metadata retrieved successfully", metadata));
        }

    /**
     * Validate rule conditions
     * POST /api/rules/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<ValidationResult>> validateRule(@Valid @RequestBody ChargeRuleCreateRequest request) {
        try {
            ValidationResult result = new ValidationResult();
            result.setValid(true);
            result.setMessage("Rule validation passed");
            
            // Basic validation logic
            if (request.getFeeValue().compareTo(java.math.BigDecimal.ZERO) < 0) {
                result.setValid(false);
                result.setMessage("Fee value cannot be negative");
                result.addError("feeValue", "Must be non-negative");
            }
            
            if (request.getMaxAmount() != null && request.getMinAmount() != null && 
                request.getMaxAmount().compareTo(request.getMinAmount()) <= 0) {
                result.setValid(false);
                result.setMessage("Maximum amount must be greater than minimum amount");
                result.addError("maxAmount", "Must be greater than minimum amount");
            }
            
            // Validate rule code format
            if (request.getRuleCode() != null && !request.getRuleCode().matches("^[0-9A-Z]+$")) {
                result.setValid(false);
                result.setMessage("Rule code must contain only numbers and uppercase letters");
                result.addError("ruleCode", "Invalid format");
            }
            
            // Check for duplicate rule code
            if (chargeRuleService.getRuleByCode(request.getRuleCode()).isPresent()) {
                result.setValid(false);
                result.setMessage("Rule code already exists");
                result.addError("ruleCode", "Code must be unique");
            }
            
            return ResponseEntity.ok(ApiResponse.success("Validation completed", result));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Validation failed: " + e.getMessage(), 500));
        }
    }

    /**
     * Bulk operations on rules
     * POST /api/rules/bulk-action
     */
    @PostMapping("/bulk-action")
    public ResponseEntity<ApiResponse<BulkActionResult>> performBulkAction(@RequestBody BulkActionRequest request) {
        try {
            BulkActionResult result = new BulkActionResult();
            result.setAction(request.getAction());
            result.setProcessedCount(0);
            result.setSuccessCount(0);
            result.setFailureCount(0);
            
            for (Long ruleId : request.getRuleIds()) {
                result.setProcessedCount(result.getProcessedCount() + 1);
                
                try {
                    switch (request.getAction().toUpperCase()) {
                        case "APPROVE":
                            chargeRuleService.approveRule(ruleId);
                            result.setSuccessCount(result.getSuccessCount() + 1);
                            break;
                        case "DEACTIVATE":
                            chargeRuleService.deactivateRule(ruleId);
                            result.setSuccessCount(result.getSuccessCount() + 1);
                            break;
                        case "DELETE":
                            chargeRuleService.deleteRule(ruleId);
                            result.setSuccessCount(result.getSuccessCount() + 1);
                            break;
                        default:
                            result.setFailureCount(result.getFailureCount() + 1);
                            result.addError(ruleId, "Unknown action: " + request.getAction());
                    }
                } catch (Exception e) {
                    result.setFailureCount(result.getFailureCount() + 1);
                    result.addError(ruleId, e.getMessage());
                }
            }
            
            String message = String.format("Bulk %s completed. Success: %d, Failures: %d", 
                request.getAction(), result.getSuccessCount(), result.getFailureCount());
            
            return ResponseEntity.ok(ApiResponse.success(message, result));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Bulk operation failed: " + e.getMessage(), 500));
        }
    }

    // DTO Classes for additional functionality
    
    public static class ValidationResult {
        private boolean valid;
        private String message;
        private Map<String, String> fieldErrors = new HashMap<>();
        
        public void addError(String field, String error) {
            fieldErrors.put(field, error);
        }
        
        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Map<String, String> getFieldErrors() { return fieldErrors; }
        public void setFieldErrors(Map<String, String> fieldErrors) { this.fieldErrors = fieldErrors; }
    }
    
    public static class RuleMetadata {
        private ChargeRule.Category[] categories;
        private ChargeRule.ActivityType[] activityTypes;
        private ChargeRule.FeeType[] feeTypes;
        private ChargeRule.ThresholdPeriod[] thresholdPeriods;
        private ChargeRule.Status[] statuses;
        
        // Getters and setters
        public ChargeRule.Category[] getCategories() { return categories; }
        public void setCategories(ChargeRule.Category[] categories) { this.categories = categories; }
        
        public ChargeRule.ActivityType[] getActivityTypes() { return activityTypes; }
        public void setActivityTypes(ChargeRule.ActivityType[] activityTypes) { this.activityTypes = activityTypes; }
        
        public ChargeRule.FeeType[] getFeeTypes() { return feeTypes; }
        public void setFeeTypes(ChargeRule.FeeType[] feeTypes) { this.feeTypes = feeTypes; }
        
        public ChargeRule.ThresholdPeriod[] getThresholdPeriods() { return thresholdPeriods; }
        public void setThresholdPeriods(ChargeRule.ThresholdPeriod[] thresholdPeriods) { this.thresholdPeriods = thresholdPeriods; }
        
        public ChargeRule.Status[] getStatuses() { return statuses; }
        public void setStatuses(ChargeRule.Status[] statuses) { this.statuses = statuses; }
    }
    
    public static class BulkActionRequest {
        private String action;
        private List<Long> ruleIds;
        
        // Getters and setters
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public List<Long> getRuleIds() { return ruleIds; }
        public void setRuleIds(List<Long> ruleIds) { this.ruleIds = ruleIds; }
    }
    
    public static class BulkActionResult {
        private String action;
        private int processedCount;
        private int successCount;
        private int failureCount;
        private Map<Long, String> errors = new HashMap<>();
        
        public void addError(Long ruleId, String error) {
            errors.put(ruleId, error);
        }
        
        // Getters and setters
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public int getProcessedCount() { return processedCount; }
        public void setProcessedCount(int processedCount) { this.processedCount = processedCount; }
        
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        
        public int getFailureCount() { return failureCount; }
        public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
        
        public Map<Long, String> getErrors() { return errors; }
        public void setErrors(Map<Long, String> errors) { this.errors = errors; }
    }
}