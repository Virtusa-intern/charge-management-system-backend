package com.bank.charge_management_system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Schema(description = "Response containing the result of charge calculation for a transaction, including all applicable charges and total")
public class ChargeCalculationResult {

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

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public List<ChargeCalculationDetail> getCalculatedCharges() {
        return calculatedCharges;
    }

    public void setCalculatedCharges(List<ChargeCalculationDetail> calculatedCharges) {
        this.calculatedCharges = calculatedCharges;
    }

    public BigDecimal getTotalCharges() {
        return totalCharges;
    }

    public void setTotalCharges(BigDecimal totalCharges) {
        this.totalCharges = totalCharges;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCalculationTimestamp() {
        return calculationTimestamp;
    }

    public void setCalculationTimestamp(LocalDateTime calculationTimestamp) {
        this.calculationTimestamp = calculationTimestamp;
    }

    public int getApplicableRulesCount() {
        return applicableRulesCount;
    }

    public void setApplicableRulesCount(int applicableRulesCount) {
        this.applicableRulesCount = applicableRulesCount;
    }

    public String getCalculationSummary() {
        return calculationSummary;
    }

    public void setCalculationSummary(String calculationSummary) {
        this.calculationSummary = calculationSummary;
    }

    @Schema(description = "Transaction identifier", example = "TXN20240101123456")
    private String transactionId;

    @Schema(description = "Customer code", example = "CUST001")
    private String customerCode;

    @Schema(description = "Transaction type", example = "ATM_WITHDRAWAL")
    private String transactionType;

    @Schema(description = "Original transaction amount", example = "5000.00")
    private BigDecimal transactionAmount;

    @Schema(description = "Transaction channel", example = "ATM", allowableValues = { "ATM", "ONLINE", "BRANCH",
            "MOBILE", "API" })
    private String channel; // Store the channel from request

    @Schema(description = "List of all calculated charges for this transaction")
    private List<ChargeCalculationDetail> calculatedCharges = new ArrayList<>();

    @Schema(description = "Total charges amount", example = "125.00")
    private BigDecimal totalCharges = BigDecimal.ZERO;

    @Schema(description = "Whether calculation was successful", example = "true")
    private boolean success;

    @Schema(description = "Calculation status message", example = "Charges calculated successfully")
    private String message;

    @Schema(description = "Timestamp when calculation was performed", example = "2024-01-15T14:30:00", format = "date-time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime calculationTimestamp;

    // Summary information
    @Schema(description = "Number of rules that were applicable", example = "2")
    private int applicableRulesCount;

    @Schema(description = "Human-readable summary of calculation", example = "Applied 2 rule(s). Total: ₹125.00 (ATMWD001: ₹100.00 + SRVCHG01: ₹25.00)")
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