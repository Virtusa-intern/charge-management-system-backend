package com.bank.charge_management_system.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "charge_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ChargeRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "rule_code", unique = true, nullable = false, length = 10)
    private String ruleCode;
    
    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conditions_json", nullable = false)
    private Map<String, Object> conditions;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false)
    private FeeType feeType;
    
    @Column(name = "fee_value", nullable = false, precision = 10, scale = 4)
    private BigDecimal feeValue;
    
    @Column(name = "currency_code", length = 3)
    private String currencyCode = "INR";
    
    @Column(name = "min_amount", precision = 15, scale = 2)
    private BigDecimal minAmount = BigDecimal.ZERO;
    
    @Column(name = "max_amount", precision = 15, scale = 2)
    private BigDecimal maxAmount;
    
    @Column(name = "threshold_count")
    private Integer thresholdCount = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "threshold_period")
    private ThresholdPeriod thresholdPeriod = ThresholdPeriod.MONTHLY;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.DRAFT;
    
    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom = LocalDateTime.now();
    
    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    // Enums
    public enum Category {
        RETAIL_BANKING, CORP_BANKING, ALL
    }
    
    public enum ActivityType {
        UNIT_WISE, RANGE_BASED, MONTHLY, SPECIAL, ADHOC
    }
    
    public enum FeeType {
        PERCENTAGE, FLAT_AMOUNT, TIERED
    }
    
    public enum ThresholdPeriod {
        DAILY, MONTHLY, YEARLY
    }
    
    public enum Status {
        DRAFT, ACTIVE, INACTIVE, ARCHIVED
    }
}