
package com.bank.charge_management_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Response containing transaction history details")
public class TransactionHistoryDto {

    @Schema(description = "Unique transaction identifier", example = "TXN20240101123456")
    private String transactionId;

    @Schema(description = "Type of transaction", example = "ATM_WITHDRAWAL")
    private String transactionType;

    @Schema(description = "Transaction amount", example = "5000.00")
    private BigDecimal amount;

    @Schema(description = "Date and time of transaction", example = "2024-01-15T14:30:00", format = "date-time")
    private LocalDateTime transactionDate;

    @Schema(description = "Channel used for transaction", example = "ATM", allowableValues = { "ATM", "ONLINE",
            "BRANCH", "MOBILE", "API" })
    private String channel;

    @Schema(description = "Transaction status", example = "COMPLETED", allowableValues = { "PENDING", "COMPLETED",
            "FAILED", "REVERSED" })
    private String status;

    public TransactionHistoryDto(String transactionId, String transactionType, BigDecimal amount,
            LocalDateTime transactionDate, String channel, String status) {
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.channel = channel;
        this.status = status;
    }

    // Getters and setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
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

    public LocalDateTime getTransactionDate() {
        return transactionDate;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
