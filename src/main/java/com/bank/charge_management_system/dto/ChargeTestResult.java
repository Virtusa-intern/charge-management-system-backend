package com.bank.charge_management_system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class ChargeTestResult {
    
    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public List<TransactionTestResult> getTransactionResults() {
        return transactionResults;
    }

    public void setTransactionResults(List<TransactionTestResult> transactionResults) {
        this.transactionResults = transactionResults;
    }

    public BigDecimal getTotalChargesAcrossAllTransactions() {
        return totalChargesAcrossAllTransactions;
    }

    public void setTotalChargesAcrossAllTransactions(BigDecimal totalChargesAcrossAllTransactions) {
        this.totalChargesAcrossAllTransactions = totalChargesAcrossAllTransactions;
    }

    public int getTotalTransactionsTested() {
        return totalTransactionsTested;
    }

    public void setTotalTransactionsTested(int totalTransactionsTested) {
        this.totalTransactionsTested = totalTransactionsTested;
    }

    public int getTransactionsWithCharges() {
        return transactionsWithCharges;
    }

    public void setTransactionsWithCharges(int transactionsWithCharges) {
        this.transactionsWithCharges = transactionsWithCharges;
    }

    public LocalDateTime getTestTimestamp() {
        return testTimestamp;
    }

    public void setTestTimestamp(LocalDateTime testTimestamp) {
        this.testTimestamp = testTimestamp;
    }

    public boolean isTestSuccessful() {
        return testSuccessful;
    }

    public void setTestSuccessful(boolean testSuccessful) {
        this.testSuccessful = testSuccessful;
    }

    public String getTestSummary() {
        return testSummary;
    }

    public void setTestSummary(String testSummary) {
        this.testSummary = testSummary;
    }

    public String getTestDescription() {
        return testDescription;
    }

    public void setTestDescription(String testDescription) {
        this.testDescription = testDescription;
    }

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
    
    public static class TransactionTestResult {
        private String transactionType;
        private BigDecimal transactionAmount;
        private String channel;
        private String description;
        
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

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<ChargeCalculationDetail> getApplicableCharges() {
            return applicableCharges;
        }

        public void setApplicableCharges(List<ChargeCalculationDetail> applicableCharges) {
            this.applicableCharges = applicableCharges;
        }

        public BigDecimal getTotalChargeForTransaction() {
            return totalChargeForTransaction;
        }

        public void setTotalChargeForTransaction(BigDecimal totalChargeForTransaction) {
            this.totalChargeForTransaction = totalChargeForTransaction;
        }

        public int getRulesApplied() {
            return rulesApplied;
        }

        public void setRulesApplied(int rulesApplied) {
            this.rulesApplied = rulesApplied;
        }

        public String getCalculationSummary() {
            return calculationSummary;
        }

        public void setCalculationSummary(String calculationSummary) {
            this.calculationSummary = calculationSummary;
        }

        public boolean isCalculationSuccessful() {
            return calculationSuccessful;
        }

        public void setCalculationSuccessful(boolean calculationSuccessful) {
            this.calculationSuccessful = calculationSuccessful;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

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