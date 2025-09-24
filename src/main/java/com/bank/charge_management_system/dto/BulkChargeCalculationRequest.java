package com.bank.charge_management_system.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkChargeCalculationRequest {
    
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