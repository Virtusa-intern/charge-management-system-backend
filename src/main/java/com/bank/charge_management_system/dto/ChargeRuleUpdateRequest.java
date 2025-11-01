package com.bank.charge_management_system.dto;

import com.bank.charge_management_system.entity.ChargeRule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Request body for updating an existing charge rule. All fields remain the same as creation.")
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

    @Schema(description = "Unique rule code identifier", example = "ATMWD001", maxLength = 50, pattern = "^[0-9A-Z]*$")
    @Size(max = 50, message = "Rule code must not exceed 50 characters")
    @Pattern(regexp = "^[0-9A-Z]*$", message = "Rule code must contain only numbers and uppercase letters")
    private String ruleCode;

    @Schema(description = "Human-readable name for the rule", example = "ATM Withdrawal Charge - Updated", required = true, maxLength = 100)
    @NotBlank(message = "Rule name is required")
    @Size(max = 100, message = "Rule name must not exceed 100 characters")
    private String ruleName;

    @Schema(description = "Category of the charge rule", example = "TRANSACTIONAL", required = true, allowableValues = {
            "TRANSACTIONAL", "ACCOUNT_MAINTENANCE", "PENALTY", "SERVICE" })
    @NotNull(message = "Category is required")
    private ChargeRule.Category category;

    @Schema(description = "Type of banking activity this rule applies to", example = "ATM_WITHDRAWAL", required = true, allowableValues = {
            "ATM_WITHDRAWAL", "ONLINE_TRANSFER", "BRANCH_DEPOSIT", "MOBILE_PAYMENT", "INTERNATIONAL_TRANSFER",
            "CHEQUE_BOOK_REQUEST", "ACCOUNT_STATEMENT", "SMS_ALERT", "MINIMUM_BALANCE_PENALTY", "LATE_PAYMENT" })
    @NotNull(message = "Activity type is required")
    private ChargeRule.ActivityType activityType;

    @Schema(description = "JSON object containing rule conditions", example = "{\"customerType\": \"RETAIL\", \"minTransactionAmount\": 1000}", required = true)
    @NotNull(message = "Conditions are required")
    private Map<String, Object> conditions;

    @Schema(description = "Type of fee calculation method", example = "PERCENTAGE", required = true, allowableValues = {
            "PERCENTAGE", "FLAT_AMOUNT", "TIERED" })
    @NotNull(message = "Fee type is required")
    private ChargeRule.FeeType feeType;

    @Schema(description = "Fee value", example = "3.0000", required = true, minimum = "0")
    @NotNull(message = "Fee value is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Fee value must be non-negative")
    @Digits(integer = 6, fraction = 4, message = "Fee value must have at most 6 integer and 4 decimal places")
    private BigDecimal feeValue;

    @Schema(description = "Currency code for the charge", example = "INR", maxLength = 3)
    private String currencyCode;

    @Schema(description = "Minimum charge amount to apply", example = "15.00", minimum = "0")
    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum amount must be non-negative")
    private BigDecimal minAmount;

    @Schema(description = "Maximum charge amount cap", example = "600.00")
    private BigDecimal maxAmount;

    @Schema(description = "Number of free transactions before charges apply", example = "3", minimum = "0")
    @Min(value = 0, message = "Threshold count must be non-negative")
    private Integer thresholdCount;

    @Schema(description = "Time period for threshold counting", example = "MONTHLY", allowableValues = { "DAILY",
            "WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY" })
    private ChargeRule.ThresholdPeriod thresholdPeriod;

    @Schema(description = "Rule effective start date-time", example = "2024-01-01T00:00:00", format = "date-time")
    private LocalDateTime effectiveFrom;

    @Schema(description = "Rule effective end date-time", example = "2024-12-31T23:59:59", format = "date-time")
    private LocalDateTime effectiveTo;
}
