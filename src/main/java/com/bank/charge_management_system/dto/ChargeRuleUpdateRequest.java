package com.bank.charge_management_system.dto;

import com.bank.charge_management_system.entity.ChargeRule;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargeRuleUpdateRequest {
    
    @NotBlank(message = "Rule name is required")
    @Size(max = 100, message = "Rule name must not exceed 100 characters")
    private String ruleName;
    
    @NotNull(message = "Category is required")
    private ChargeRule.Category category;
    
    @NotNull(message = "Activity type is required")
    private ChargeRule.ActivityType activityType;
    
    @NotNull(message = "Conditions are required")
    private Map<String, Object> conditions;
    
    @NotNull(message = "Fee type is required")
    private ChargeRule.FeeType feeType;
    
    @NotNull(message = "Fee value is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Fee value must be non-negative")
    @Digits(integer = 6, fraction = 4, message = "Fee value must have at most 6 integer and 4 decimal places")
    private BigDecimal feeValue;
    
    private String currencyCode;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum amount must be non-negative")
    private BigDecimal minAmount;
    
    private BigDecimal maxAmount;
    
    @Min(value = 0, message = "Threshold count must be non-negative")
    private Integer thresholdCount;
    
    private ChargeRule.ThresholdPeriod thresholdPeriod;
    
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
}
