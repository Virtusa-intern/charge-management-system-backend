package com.bank.charge_management_system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class ChargeCalculationResult {
    
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public List<ChargeCalculationDetail> getCalculatedCharges() {
        return calculatedCharges;
    }

    public void setCalculatedCharges(List<ChargeCalculationDetail> calculatedCharges) {
        this.calculatedCharges = calculatedCharges;
    }

    public BigDecimal getTotalCharges() {
        return totalCharges;
    }

    public void setTotalCharges(BigDecimal totalCharges) {
        this.totalCharges = totalCharges;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCalculationTimestamp() {
        return calculationTimestamp;
    }

    public void setCalculationTimestamp(LocalDateTime calculationTimestamp) {
        this.calculationTimestamp = calculationTimestamp;
    }

    public int getApplicableRulesCount() {
        return applicableRulesCount;
    }

    public void setApplicableRulesCount(int applicableRulesCount) {
        this.applicableRulesCount = applicableRulesCount;
    }

    public String getCalculationSummary() {
        return calculationSummary;
    }

    public void setCalculationSummary(String calculationSummary) {
        this.calculationSummary = calculationSummary;
    }

    private String transactionId;
    private String customerCode;
    private String transactionType;
    private BigDecimal transactionAmount;
    
    private List<ChargeCalculationDetail> calculatedCharges = new ArrayList<>();
    private BigDecimal totalCharges = BigDecimal.ZERO;
    
    private boolean success;
    private String message;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime calculationTimestamp;
    
    // Summary information
    private int applicableRulesCount;
    private String calculationSummary;
    
    // Helper method to add charge
    public void addCharge(ChargeCalculationDetail charge) {
        if (this.calculatedCharges == null) {
            this.calculatedCharges = new ArrayList<>();
        }
        this.calculatedCharges.add(charge);
        if (this.totalCharges == null) {
            this.totalCharges = BigDecimal.ZERO;
        }
        this.totalCharges = this.totalCharges.add(charge.getChargeAmount());
        this.applicableRulesCount = this.calculatedCharges.size();
    }
    
    // Helper method to build summary
    public void generateSummary() {
        if (calculatedCharges == null || calculatedCharges.isEmpty()) {
            this.calculationSummary = "No charges applicable for this transaction";
        } else {
            StringBuilder summary = new StringBuilder();
            summary.append("Applied ").append(calculatedCharges.size()).append(" rule(s). ");
            summary.append("Total: ₹").append(totalCharges).append(" (");
            for (int i = 0; i < calculatedCharges.size(); i++) {
                ChargeCalculationDetail charge = calculatedCharges.get(i);
                summary.append(charge.getRuleCode()).append(": ₹").append(charge.getChargeAmount());
                if (i < calculatedCharges.size() - 1) {
                    summary.append(" + ");
                }
            }
            summary.append(")");
            this.calculationSummary = summary.toString();
        }
    }
}