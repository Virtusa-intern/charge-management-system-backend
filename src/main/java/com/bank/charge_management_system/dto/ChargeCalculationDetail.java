package com.bank.charge_management_system.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargeCalculationDetail {
    
    private Long ruleId;
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