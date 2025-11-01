package com.bank.charge_management_system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for ChargeRule responses
 */
@Schema(description = "Response containing complete charge rule details including audit information")
public class ChargeRuleDto {

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public String getThresholdPeriod() {
        return thresholdPeriod;
    }

    public void setThresholdPeriod(String thresholdPeriod) {
        this.thresholdPeriod = thresholdPeriod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getConditions() {
        return conditions;
    }

    public void setConditions(Map<String, Object> conditions) {
        this.conditions = conditions;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Long getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Long approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    @Schema(description = "Unique database identifier for the rule", example = "1")
    private Long id;

    @Schema(description = "Unique rule code", example = "ATMWD001")
    private String ruleCode;

    @Schema(description = "Human-readable rule name", example = "ATM Withdrawal Charge")
    private String ruleName;

    // Enums as strings for frontend
    @Schema(description = "Category of the charge", example = "TRANSACTIONAL", allowableValues = { "TRANSACTIONAL",
            "ACCOUNT_MAINTENANCE", "PENALTY", "SERVICE" })
    private String category;

    @Schema(description = "Banking activity type", example = "ATM_WITHDRAWAL")
    private String activityType;

    @Schema(description = "Fee calculation method", example = "PERCENTAGE", allowableValues = { "PERCENTAGE",
            "FLAT_AMOUNT", "TIERED" })
    private String feeType;

    @Schema(description = "Threshold period for counting transactions", example = "MONTHLY", allowableValues = {
            "DAILY", "WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY" })
    private String thresholdPeriod;

    @Schema(description = "Current status of the rule", example = "ACTIVE", allowableValues = { "DRAFT",
            "PENDING_APPROVAL", "ACTIVE", "INACTIVE", "EXPIRED" })
    private String status;

    // Rule conditions as JSON
    @Schema(description = "JSON object with rule conditions", example = "{\"customerType\": \"RETAIL\", \"minTransactionAmount\": 1000}")
    private Map<String, Object> conditions;

    // Fee configuration
    @Schema(description = "Fee value (percentage or flat amount)", example = "2.5000")
    private BigDecimal feeValue;

    @Schema(description = "Currency code", example = "INR")
    private String currencyCode;

    // Business logic fields
    @Schema(description = "Minimum charge amount", example = "10.00")
    private BigDecimal minAmount;

    @Schema(description = "Maximum charge amount", example = "500.00")
    private BigDecimal maxAmount;

    @Schema(description = "Free transaction threshold", example = "5")
    private Integer thresholdCount;

    // Effective dates
    @Schema(description = "Rule effective from date-time", example = "2024-01-01T00:00:00", format = "date-time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime effectiveFrom;

    @Schema(description = "Rule effective until date-time", example = "2024-12-31T23:59:59", format = "date-time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime effectiveTo;

    // Audit fields
    @Schema(description = "Rule creation timestamp", example = "2024-01-01T10:30:00", format = "date-time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2024-01-15T14:20:00", format = "date-time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "User ID who created the rule", example = "1")
    private Long createdBy;

    @Schema(description = "User ID who last updated the rule", example = "2")
    private Long updatedBy;

    @Schema(description = "User ID who approved the rule", example = "3")
    private Long approvedBy;

    @Schema(description = "Rule approval timestamp", example = "2024-01-02T09:00:00", format = "date-time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime approvedAt;
}