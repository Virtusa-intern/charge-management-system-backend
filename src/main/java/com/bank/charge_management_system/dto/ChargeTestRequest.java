package com.bank.charge_management_system.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

public class ChargeTestRequest {
    
    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public List<TestTransaction> getTestTransactions() {
        return testTransactions;
    }

    public void setTestTransactions(List<TestTransaction> testTransactions) {
        this.testTransactions = testTransactions;
    }

    public boolean isSaveResults() {
        return saveResults;
    }

    public void setSaveResults(boolean saveResults) {
        this.saveResults = saveResults;
    }

    public String getTestDescription() {
        return testDescription;
    }

    public void setTestDescription(String testDescription) {
        this.testDescription = testDescription;
    }

    @NotBlank(message = "Customer code is required")
    private String customerCode;
    
    @NotNull(message = "Test transactions are required")
    @Size(min = 1, max = 10, message = "Between 1-10 test transactions allowed")
    @Valid
    private List<TestTransaction> testTransactions = new ArrayList<>();
    
    private boolean saveResults = false; // Whether to save results to database
    private String testDescription; // Optional description for the test
    
    public static class TestTransaction {
        @NotBlank(message = "Transaction type is required")
        private String transactionType;
        
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be positive")
        @DecimalMax(value = "10000000.00", message = "Amount too large")
        private BigDecimal amount;
        
        private String channel; // ATM, ONLINE, BRANCH, MOBILE, API
        private String description; // Test description
        private String sourceAccount;
        private String destinationAccount;
        
        public String getTransactionType() {
            return transactionType;
        }

        public void setTransactionType(String transactionType) {
            this.transactionType = transactionType;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
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

        public String getSourceAccount() {
            return sourceAccount;
        }

        public void setSourceAccount(String sourceAccount) {
            this.sourceAccount = sourceAccount;
        }

        public String getDestinationAccount() {
            return destinationAccount;
        }

        public void setDestinationAccount(String destinationAccount) {
            this.destinationAccount = destinationAccount;
        }

        // Constructor for easy test creation
        public TestTransaction(String transactionType, BigDecimal amount, String description) {
            this.transactionType = transactionType;
            this.amount = amount;
            this.description = description;
        }
        
        public TestTransaction(String transactionType, BigDecimal amount, String channel, String description) {
            this.transactionType = transactionType;
            this.amount = amount;
            this.channel = channel;
            this.description = description;
        }
    }
    
    // Helper method to add test transaction
    public void addTestTransaction(String transactionType, BigDecimal amount, String description) {
        if (this.testTransactions == null) {
            this.testTransactions = new ArrayList<>();
        }
        this.testTransactions.add(new TestTransaction(transactionType, amount, description));
    }
}