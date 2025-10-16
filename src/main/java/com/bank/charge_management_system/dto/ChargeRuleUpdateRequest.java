package com.bank.charge_management_system.dto;

import com.bank.charge_management_system.entity.ChargeRule;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class ChargeRuleUpdateRequest {

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }
    
    public String getRuleName() {
        return ruleName;
    }
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
    public ChargeRule.Category getCategory() {
        return category;
    }
    public void setCategory(ChargeRule.Category category) {
        this.category = category;
    }
    public ChargeRule.ActivityType getActivityType() {
        return activityType;
    }
    public void setActivityType(ChargeRule.ActivityType activityType) {
        this.activityType = activityType;
    }
    public Map<String, Object> getConditions() {
        return conditions;
    }
    public void setConditions(Map<String, Object> conditions) {
        this.conditions = conditions;
    }
    public ChargeRule.FeeType getFeeType() {
        return feeType;
    }
    public void setFeeType(ChargeRule.FeeType feeType) {
        this.feeType = feeType;
    }
    public BigDecimal getFeeValue() {
        return feeValue;
    }
    public void setFeeValue(BigDecimal feeValue) {
        this.feeValue = feeValue;
    }
    public String getCurrencyCode() {
        return currencyCode;
    }
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
    public BigDecimal getMinAmount() {
        return minAmount;
    }
    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }
    public BigDecimal getMaxAmount() {
        return maxAmount;
    }
    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }
    public Integer getThresholdCount() {
        return thresholdCount;
    }
    public void setThresholdCount(Integer thresholdCount) {
        this.thresholdCount = thresholdCount;
    }
    public ChargeRule.ThresholdPeriod getThresholdPeriod() {
        return thresholdPeriod;
    }
    public void setThresholdPeriod(ChargeRule.ThresholdPeriod thresholdPeriod) {
        this.thresholdPeriod = thresholdPeriod;
    }
    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }
    public void setEffectiveFrom(LocalDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }
    public LocalDateTime getEffectiveTo() {
        return effectiveTo;
    }
    public void setEffectiveTo(LocalDateTime effectiveTo) {
        this.effectiveTo = effectiveTo;
    }
    @Size(max = 50, message = "Rule code must not exceed 50 characters")
    @Pattern(regexp = "^[0-9A-Z]*$", message = "Rule code must contain only numbers and uppercase letters")
    private String ruleCode;

    @NotBlank(message = "Rule name is required")
    @Size(max = 100, message = "Rule name must not exceed 100 characters")
    private String ruleName;
    
    @NotNull(message = "Category is required")
    private ChargeRule.Category category;
    
    @NotNull(message = "Activity type is required")
    private ChargeRule.ActivityType activityType;
    
    @NotNull(message = "Conditions are required")
    private Map<String, Object> conditions;
    
    @NotNull(message = "Fee type is required")
    private ChargeRule.FeeType feeType;
    
    @NotNull(message = "Fee value is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Fee value must be non-negative")
    @Digits(integer = 6, fraction = 4, message = "Fee value must have at most 6 integer and 4 decimal places")
    private BigDecimal feeValue;
    
    private String currencyCode;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum amount must be non-negative")
    private BigDecimal minAmount;
    
    private BigDecimal maxAmount;
    
    @Min(value = 0, message = "Threshold count must be non-negative")
    private Integer thresholdCount;
    
    private ChargeRule.ThresholdPeriod thresholdPeriod;
    
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
}
