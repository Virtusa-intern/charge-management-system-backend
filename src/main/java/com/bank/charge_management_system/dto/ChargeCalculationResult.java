package com.bank.charge_management_system.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargeCalculationResult {
    
    private String transactionId;
    private String customerCode;
    private String transactionType;
    private BigDecimal transactionAmount;
    
    private List<ChargeCalculationDetail> calculatedCharges = new ArrayList<>();
    private BigDecimal totalCharges = BigDecimal.ZERO;
    
    private boolean success;
    private String message;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime calculationTimestamp;
    
    // Summary information
    private int applicableRulesCount;
    private String calculationSummary;
    
    // Helper method to add charge
    public void addCharge(ChargeCalculationDetail charge) {
        if (this.calculatedCharges == null) {
            this.calculatedCharges = new ArrayList<>();
        }
        this.calculatedCharges.add(charge);
        if (this.totalCharges == null) {
            this.totalCharges = BigDecimal.ZERO;
        }
        this.totalCharges = this.totalCharges.add(charge.getChargeAmount());
        this.applicableRulesCount = this.calculatedCharges.size();
    }
    
    // Helper method to build summary
    public void generateSummary() {
        if (calculatedCharges == null || calculatedCharges.isEmpty()) {
            this.calculationSummary = "No charges applicable for this transaction";
        } else {
            StringBuilder summary = new StringBuilder();
            summary.append("Applied ").append(calculatedCharges.size()).append(" rule(s). ");
            summary.append("Total: ₹").append(totalCharges).append(" (");
            for (int i = 0; i < calculatedCharges.size(); i++) {
                ChargeCalculationDetail charge = calculatedCharges.get(i);
                summary.append(charge.getRuleCode()).append(": ₹").append(charge.getChargeAmount());
                if (i < calculatedCharges.size() - 1) {
                    summary.append(" + ");
                }
            }
            summary.append(")");
            this.calculationSummary = summary.toString();
        }
    }
}