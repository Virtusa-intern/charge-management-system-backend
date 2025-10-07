// src/main/java/com/bank/chargemgmt/dto/TransactionRequest.java
package com.bank.charge_management_system.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
    
    @NotBlank(message = "Customer code is required")
    private String customerCode;
    
    @NotBlank(message = "Transaction type is required")
    private String transactionType;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;
    
    private String currencyCode = "INR";
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", shape = JsonFormat.Shape.STRING, timezone = "UTC")
    private LocalDateTime transactionDate;
    
    private String channel; // ATM, ONLINE, BRANCH, MOBILE, API
    
    private String sourceAccount;
    private String destinationAccount;
    
    // Additional metadata for complex rule evaluation
    private Map<String, Object> metadata;
    
    // Constructor for quick testing
    public TransactionRequest(String transactionId, String customerCode, String transactionType, BigDecimal amount) {
        this.transactionId = transactionId;
        this.customerCode = customerCode;
        this.transactionType = transactionType;
        this.amount = amount;
        this.currencyCode = "INR";
        this.transactionDate = LocalDateTime.now();
    }
    
    // Constructor with channel
    public TransactionRequest(String transactionId, String customerCode, String transactionType, BigDecimal amount, String channel) {
        this(transactionId, customerCode, transactionType, amount);
        this.channel = channel;
    }

    // Ensure transactionDate is set if not provided
    public LocalDateTime getTransactionDate() {
        return transactionDate != null ? transactionDate : LocalDateTime.now();
    }
}