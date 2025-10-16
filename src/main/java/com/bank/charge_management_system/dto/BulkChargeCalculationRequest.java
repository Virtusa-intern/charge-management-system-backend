package com.bank.charge_management_system.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.ArrayList;

public class BulkChargeCalculationRequest {
    
    public List<TransactionRequest> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionRequest> transactions) {
        this.transactions = transactions;
    }

    public boolean isSaveResults() {
        return saveResults;
    }

    public void setSaveResults(boolean saveResults) {
        this.saveResults = saveResults;
    }

    public boolean isStopOnError() {
        return stopOnError;
    }

    public void setStopOnError(boolean stopOnError) {
        this.stopOnError = stopOnError;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NotNull(message = "Transactions are required")
    @Size(min = 1, max = 100, message = "Bulk calculation supports 1-100 transactions")
    @Valid
    private List<TransactionRequest> transactions = new ArrayList<>();
    
    private boolean saveResults = true; // Whether to save results to database
    private boolean stopOnError = false; // Whether to stop processing on first error
    private String batchId; // Optional batch identifier
    private String description; // Optional batch description
    
    // Helper method to add transaction
    public void addTransaction(TransactionRequest transaction) {
        if (this.transactions == null) {
            this.transactions = new ArrayList<>();
        }
        this.transactions.add(transaction);
    }
    
    // Helper method to create transaction and add
    public void addTransaction(String transactionId, String customerCode, String transactionType, 
                             java.math.BigDecimal amount, String channel) {
        TransactionRequest request = new TransactionRequest(transactionId, customerCode, transactionType, amount, channel);
        addTransaction(request);
    }
}