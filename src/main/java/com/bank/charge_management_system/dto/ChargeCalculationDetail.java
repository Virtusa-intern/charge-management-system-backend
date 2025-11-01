package com.bank.charge_management_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Detailed information about a single charge calculation, including the rule applied and resulting charge amount")
public class ChargeCalculationDetail {

    public ChargeCalculationDetail() {

    }

    @Schema(description = "Database ID of the rule", example = "1")
    private Long ruleId;

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
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

    public String getRuleCategory() {
        return ruleCategory;
    }

    public void setRuleCategory(String ruleCategory) {
        this.ruleCategory = ruleCategory;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public BigDecimal getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(BigDecimal chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public String getChargeCurrency() {
        return chargeCurrency;
    }

    public void setChargeCurrency(String chargeCurrency) {
        this.chargeCurrency = chargeCurrency;
    }

    public String getCalculationBasis() {
        return calculationBasis;
    }

    public void setCalculationBasis(String calculationBasis) {
        this.calculationBasis = calculationBasis;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public BigDecimal getAppliedRate() {
        return appliedRate;
    }

    public void setAppliedRate(BigDecimal appliedRate) {
        this.appliedRate = appliedRate;
    }

    public Integer getThresholdCount() {
        return thresholdCount;
    }

    public void setThresholdCount(Integer thresholdCount) {
        this.thresholdCount = thresholdCount;
    }

    public String getThresholdPeriod() {
        return thresholdPeriod;
    }

    public void setThresholdPeriod(String thresholdPeriod) {
        this.thresholdPeriod = thresholdPeriod;
    }

    @Schema(description = "Rule code", example = "ATMWD001")
    private String ruleCode;

    @Schema(description = "Rule name", example = "ATM Withdrawal Charge")
    private String ruleName;

    @Schema(description = "Rule category", example = "TRANSACTIONAL")
    private String ruleCategory;

    @Schema(description = "Activity type", example = "ATM_WITHDRAWAL")
    private String activityType;

    @Schema(description = "Calculated charge amount", example = "100.00")
    private BigDecimal chargeAmount = BigDecimal.ZERO;

    @Schema(description = "Currency code for the charge", example = "INR", defaultValue = "INR")
    private String chargeCurrency = "INR";

    // Explanation of how the charge was calculated
    @Schema(description = "Explanation of how charge was calculated", example = "2.5% of transaction amount (₹5000.00)")
    private String calculationBasis;

    // Additional context
    @Schema(description = "Type of fee calculation", example = "PERCENTAGE", allowableValues = { "PERCENTAGE",
            "FLAT_AMOUNT", "TIERED" })
    private String feeType; // PERCENTAGE, FLAT_AMOUNT, TIERED

    @Schema(description = "Applied rate (for percentage-based charges)", example = "2.50")
    private BigDecimal appliedRate;

    @Schema(description = "Threshold count for this rule", example = "5")
    private Integer thresholdCount;

    @Schema(description = "Threshold period", example = "MONTHLY")
    private String thresholdPeriod;

    // Simplified constructor for basic charge
    public ChargeCalculationDetail(String ruleCode, String ruleName, BigDecimal chargeAmount, String calculationBasis) {
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.chargeAmount = chargeAmount;
        this.calculationBasis = calculationBasis;
        this.chargeCurrency = "INR";
    }

    // Constructor with rule details
    public ChargeCalculationDetail(Long ruleId, String ruleCode, String ruleName, String activityType,
            BigDecimal chargeAmount, String calculationBasis) {
        this.ruleId = ruleId;
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.activityType = activityType;
        this.chargeAmount = chargeAmount;
        this.calculationBasis = calculationBasis;
        this.chargeCurrency = "INR";
    }
}