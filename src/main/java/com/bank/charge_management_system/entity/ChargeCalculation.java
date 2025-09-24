package com.bank.charge_management_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "charge_calculations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ChargeCalculation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;
    
    @Column(name = "rule_id", nullable = false)
    private Long ruleId;
    
    @Column(name = "calculated_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal calculatedAmount;
    
    @Column(name = "currency_code", length = 3)
    private String currencyCode = "INR";
    
    @Column(name = "calculation_basis", length = 500)
    private String calculationBasis;
    
    @Column(name = "threshold_count_used")
    private Integer thresholdCountUsed = 0;
    
    @Column(name = "period_start")
    private LocalDate periodStart;
    
    @Column(name = "period_end")
    private LocalDate periodEnd;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.CALCULATED;
    
    @Column(name = "applied_at")
    private LocalDateTime appliedAt;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", insertable = false, updatable = false)
    private Transaction transaction;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", insertable = false, updatable = false)
    private ChargeRule chargeRule;
    
    public enum Status {
        CALCULATED, APPLIED, WAIVED, REVERSED
    }
}