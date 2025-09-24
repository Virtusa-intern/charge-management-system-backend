package com.bank.charge_management_system.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargeTestResult {
    
    private String customerCode;
    private String customerName;
    private String customerType; // RETAIL, CORPORATE
    
    private List<TransactionTestResult> transactionResults = new ArrayList<>();
    
    private BigDecimal totalChargesAcrossAllTransactions = BigDecimal.ZERO;
    private int totalTransactionsTested = 0;
    private int transactionsWithCharges = 0;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime testTimestamp;
    
    private boolean testSuccessful = true;
    private String testSummary;
    private String testDescription;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionTestResult {
        private String transactionType;
        private BigDecimal transactionAmount;
        private String channel;
        private String description;
        
        private List<ChargeCalculationDetail> applicableCharges = new ArrayList<>();
        private BigDecimal totalChargeForTransaction = BigDecimal.ZERO;
        private int rulesApplied = 0;
        
        private String calculationSummary;
        private boolean calculationSuccessful = true;
        private String errorMessage;
        
        // Helper method to add charge
        public void addCharge(ChargeCalculationDetail charge) {
            if (this.applicableCharges == null) {
                this.applicableCharges = new ArrayList<>();
            }
            this.applicableCharges.add(charge);
            if (this.totalChargeForTransaction == null) {
                this.totalChargeForTransaction = BigDecimal.ZERO;
            }
            this.totalChargeForTransaction = this.totalChargeForTransaction.add(charge.getChargeAmount());
            this.rulesApplied = this.applicableCharges.size();
        }
    }
    
    // Helper method to add transaction result
    public void addTransactionResult(TransactionTestResult result) {
        if (this.transactionResults == null) {
            this.transactionResults = new ArrayList<>();
        }
        this.transactionResults.add(result);
        this.totalTransactionsTested++;
        
        if (result.getTotalChargeForTransaction().compareTo(BigDecimal.ZERO) > 0) {
            this.transactionsWithCharges++;
            if (this.totalChargesAcrossAllTransactions == null) {
                this.totalChargesAcrossAllTransactions = BigDecimal.ZERO;
            }
            this.totalChargesAcrossAllTransactions = this.totalChargesAcrossAllTransactions.add(result.getTotalChargeForTransaction());
        }
    }
    
    // Generate test summary
    public void generateTestSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Tested ").append(totalTransactionsTested).append(" transactions for customer ").append(customerCode).append(". ");
        summary.append(transactionsWithCharges).append(" transactions incurred charges totaling ₹").append(totalChargesAcrossAllTransactions).append(".");
        
        if (transactionsWithCharges == 0) {
            summary.append(" No charges applicable based on current rules and transaction history.");
        }
        
        this.testSummary = summary.toString();
    }
}