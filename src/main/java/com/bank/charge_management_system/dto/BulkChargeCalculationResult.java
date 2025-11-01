package com.bank.charge_management_system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Schema(description = "Response containing results of bulk charge calculation, including statistics and per-transaction results")
public class BulkChargeCalculationResult {

    @Schema(description = "Total number of transactions processed", example = "50")
    private int totalTransactions = 0;

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(int totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public int getSuccessfulCalculations() {
        return successfulCalculations;
    }

    public void setSuccessfulCalculations(int successfulCalculations) {
        this.successfulCalculations = successfulCalculations;
    }

    public int getFailedCalculations() {
        return failedCalculations;
    }

    public void setFailedCalculations(int failedCalculations) {
        this.failedCalculations = failedCalculations;
    }

    public List<ChargeCalculationResult> getResults() {
        return results;
    }

    public void setResults(List<ChargeCalculationResult> results) {
        this.results = results;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    public BigDecimal getTotalChargesCalculated() {
        return totalChargesCalculated;
    }

    public void setTotalChargesCalculated(BigDecimal totalChargesCalculated) {
        this.totalChargesCalculated = totalChargesCalculated;
    }

    public LocalDateTime getProcessingTimestamp() {
        return processingTimestamp;
    }

    public void setProcessingTimestamp(LocalDateTime processingTimestamp) {
        this.processingTimestamp = processingTimestamp;
    }

    public boolean isOverallSuccess() {
        return overallSuccess;
    }

    public void setOverallSuccess(boolean overallSuccess) {
        this.overallSuccess = overallSuccess;
    }

    public String getProcessingMessage() {
        return processingMessage;
    }

    public void setProcessingMessage(String processingMessage) {
        this.processingMessage = processingMessage;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
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

    public Map<String, Integer> getTransactionTypeCount() {
        return transactionTypeCount;
    }

    public void setTransactionTypeCount(Map<String, Integer> transactionTypeCount) {
        this.transactionTypeCount = transactionTypeCount;
    }

    public Map<String, BigDecimal> getChargesByRule() {
        return chargesByRule;
    }

    public void setChargesByRule(Map<String, BigDecimal> chargesByRule) {
        this.chargesByRule = chargesByRule;
    }

    @Schema(description = "Number of successful calculations", example = "48")
    private int successfulCalculations = 0;

    @Schema(description = "Number of failed calculations", example = "2")
    private int failedCalculations = 0;

    @Schema(description = "List of calculation results for each transaction")
    private List<ChargeCalculationResult> results = new ArrayList<>();

    @Schema(description = "Map of transaction IDs to error messages for failed transactions", example = "{\"TXN001\": \"Invalid customer code\"}")
    private Map<String, String> errors = new HashMap<>(); // transactionId -> error message

    @Schema(description = "Total charges calculated across all successful transactions", example = "5250.00")
    private BigDecimal totalChargesCalculated = BigDecimal.ZERO;

    @Schema(description = "Batch processing timestamp", example = "2024-01-15T14:30:00", format = "date-time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processingTimestamp;

    @Schema(description = "Whether overall batch processing was successful", example = "true")
    private boolean overallSuccess = true;

    @Schema(description = "Processing summary message", example = "Processed 50 transactions in 1250ms. Success: 48, Failed: 2. Total charges calculated: ₹5250.00")
    private String processingMessage;

    @Schema(description = "Total processing time in milliseconds", example = "1250")
    private long processingTimeMs = 0;

    @Schema(description = "Batch identifier", example = "BATCH20240115001")
    private String batchId;

    @Schema(description = "Batch description", example = "End-of-day transaction processing")
    private String description;

    // Processing statistics
    @Schema(description = "Count of transactions by type", example = "{\"ATM_WITHDRAWAL\": 30, \"ONLINE_TRANSFER\": 20}")
    private Map<String, Integer> transactionTypeCount = new HashMap<>();

    @Schema(description = "Total charges by rule code", example = "{\"ATMWD001\": 3000.00, \"SRVCHG01\": 2250.00}")
    private Map<String, BigDecimal> chargesByRule = new HashMap<>();

    // Helper methods
    public void addSuccessfulResult(ChargeCalculationResult result) {
        if (this.results == null) {
            this.results = new ArrayList<>();
        }
        this.results.add(result);
        this.successfulCalculations++;

        if (this.totalChargesCalculated == null) {
            this.totalChargesCalculated = BigDecimal.ZERO;
        }
        this.totalChargesCalculated = this.totalChargesCalculated.add(result.getTotalCharges());

        // Count transaction types
        String txType = result.getTransactionType();
        transactionTypeCount.put(txType, transactionTypeCount.getOrDefault(txType, 0) + 1);

        // Sum charges by rule
        for (ChargeCalculationDetail charge : result.getCalculatedCharges()) {
            String ruleCode = charge.getRuleCode();
            chargesByRule.put(ruleCode,
                    chargesByRule.getOrDefault(ruleCode, BigDecimal.ZERO).add(charge.getChargeAmount()));
        }
    }

    public void addFailedResult(String transactionId, String errorMessage) {
        if (this.errors == null) {
            this.errors = new HashMap<>();
        }
        this.errors.put(transactionId, errorMessage);
        this.failedCalculations++;
        this.overallSuccess = false;
    }

    public void generateProcessingMessage() {
        StringBuilder message = new StringBuilder();
        message.append("Processed ").append(totalTransactions).append(" transactions in ").append(processingTimeMs)
                .append("ms. ");
        message.append("Success: ").append(successfulCalculations).append(", Failed: ").append(failedCalculations)
                .append(". ");
        message.append("Total charges calculated: ₹").append(totalChargesCalculated);

        if (failedCalculations > 0) {
            message.append(". ").append(failedCalculations).append(" transactions failed processing.");
        }

        this.processingMessage = message.toString();
    }
}