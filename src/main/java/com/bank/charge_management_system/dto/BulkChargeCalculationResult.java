package com.bank.charge_management_system.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkChargeCalculationResult {
    
    private int totalTransactions = 0;
    private int successfulCalculations = 0;
    private int failedCalculations = 0;
    
    private List<ChargeCalculationResult> results = new ArrayList<>();
    private Map<String, String> errors = new HashMap<>(); // transactionId -> error message
    
    private BigDecimal totalChargesCalculated = BigDecimal.ZERO;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processingTimestamp;
    
    private boolean overallSuccess = true;
    private String processingMessage;
    private long processingTimeMs = 0;
    
    private String batchId;
    private String description;
    
    // Processing statistics
    private Map<String, Integer> transactionTypeCount = new HashMap<>();
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
            chargesByRule.put(ruleCode, chargesByRule.getOrDefault(ruleCode, BigDecimal.ZERO).add(charge.getChargeAmount()));
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
        message.append("Processed ").append(totalTransactions).append(" transactions in ").append(processingTimeMs).append("ms. ");
        message.append("Success: ").append(successfulCalculations).append(", Failed: ").append(failedCalculations).append(". ");
        message.append("Total charges calculated: ₹").append(totalChargesCalculated);
        
        if (failedCalculations > 0) {
            message.append(". ").append(failedCalculations).append(" transactions failed processing.");
        }
        
        this.processingMessage = message.toString();
    }
}