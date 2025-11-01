// src/main/java/com/bank/chargemgmt/dto/TransactionRequest.java
package com.bank.charge_management_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Request body for submitting a transaction for charge calculation. Contains all transaction details needed for rule evaluation.")
public class TransactionRequest {

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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Schema(description = "Unique transaction identifier", example = "TXN20240101123456", required = true)
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    public TransactionRequest() {

    }

    @Schema(description = "Customer code/account number", example = "CUST001", required = true)
    @NotBlank(message = "Customer code is required")
    private String customerCode;

    @Schema(description = "Type of transaction", example = "ATM_WITHDRAWAL", required = true, allowableValues = {
            "ATM_WITHDRAWAL", "ONLINE_TRANSFER", "BRANCH_DEPOSIT", "MOBILE_PAYMENT", "INTERNATIONAL_TRANSFER",
            "CHEQUE_BOOK_REQUEST", "ACCOUNT_STATEMENT" })
    @NotBlank(message = "Transaction type is required")
    private String transactionType;

    @Schema(description = "Transaction amount", example = "5000.00", required = true, minimum = "0.01")
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @Schema(description = "Currency code for the transaction", example = "INR", defaultValue = "INR", maxLength = 3)
    private String currencyCode = "INR";

    @Schema(description = "Transaction date and time (ISO 8601 format)", example = "2024-01-15T14:30:00.000Z", format = "date-time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", shape = JsonFormat.Shape.STRING, timezone = "UTC")
    private LocalDateTime transactionDate;

    @Schema(description = "Channel through which transaction was initiated", example = "ATM", allowableValues = { "ATM",
            "ONLINE", "BRANCH", "MOBILE", "API" })
    private String channel; // ATM, ONLINE, BRANCH, MOBILE, API

    @Schema(description = "Source account number", example = "ACC123456789")
    private String sourceAccount;

    @Schema(description = "Destination account number", example = "ACC987654321")
    private String destinationAccount;

    // Additional metadata for complex rule evaluation
    @Schema(description = "Additional metadata for complex rule evaluation", example = "{\"deviceId\": \"ATM001\", \"location\": \"Mumbai\"}")
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
    public TransactionRequest(String transactionId, String customerCode, String transactionType, BigDecimal amount,
            String channel) {
        this(transactionId, customerCode, transactionType, amount);
        this.channel = channel;
    }

    // Ensure transactionDate is set if not provided
    public LocalDateTime getTransactionDate() {
        return transactionDate != null ? transactionDate : LocalDateTime.now();
    }
}