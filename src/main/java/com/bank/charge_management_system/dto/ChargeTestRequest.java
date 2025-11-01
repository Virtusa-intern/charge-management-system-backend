package com.bank.charge_management_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Schema(description = "Request to test charge calculations for multiple transactions at once. Useful for rule testing and validation.")
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

    @Schema(description = "Customer code for testing", example = "CUST001", required = true)
    @NotBlank(message = "Customer code is required")
    private String customerCode;

    @Schema(description = "List of test transactions (1-10 allowed)", required = true, minLength = 1, maxLength = 10)
    @NotNull(message = "Test transactions are required")
    @Size(min = 1, max = 10, message = "Between 1-10 test transactions allowed")
    @Valid
    private List<TestTransaction> testTransactions = new ArrayList<>();

    @Schema(description = "Whether to save test results to database", example = "false", defaultValue = "false")
    private boolean saveResults = false; // Whether to save results to database

    @Schema(description = "Optional description for the test run", example = "Testing new ATM withdrawal rules")
    private String testDescription; // Optional description for the test

    @Schema(description = "Individual test transaction within a charge test")
    public static class TestTransaction {
        @Schema(description = "Type of transaction", example = "ATM_WITHDRAWAL", required = true)
        @NotBlank(message = "Transaction type is required")
        private String transactionType;

        @Schema(description = "Transaction amount", example = "5000.00", required = true, minimum = "0.01", maximum = "10000000.00")
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be positive")
        @DecimalMax(value = "10000000.00", message = "Amount too large")
        private BigDecimal amount;

        @Schema(description = "Transaction channel", example = "ATM", allowableValues = { "ATM", "ONLINE", "BRANCH",
                "MOBILE", "API" })
        private String channel; // ATM, ONLINE, BRANCH, MOBILE, API

        @Schema(description = "Test transaction description", example = "First ATM withdrawal of the month")
        private String description; // Test description

        @Schema(description = "Source account number", example = "ACC123456789")
        private String sourceAccount;

        @Schema(description = "Destination account number", example = "ACC987654321")
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