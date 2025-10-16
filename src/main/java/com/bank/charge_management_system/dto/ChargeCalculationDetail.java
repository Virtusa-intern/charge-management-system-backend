package com.bank.charge_management_system.dto;

import java.math.BigDecimal;

public class ChargeCalculationDetail {

    public ChargeCalculationDetail() {

    }
    
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

    private String ruleCode;
    private String ruleName;
    private String ruleCategory;
    private String activityType;
    
    private BigDecimal chargeAmount = BigDecimal.ZERO;
    private String chargeCurrency = "INR";
    
    // Explanation of how the charge was calculated
    private String calculationBasis;
    
    // Additional context
    private String feeType; // PERCENTAGE, FLAT_AMOUNT, TIERED
    private BigDecimal appliedRate;
    private Integer thresholdCount;
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