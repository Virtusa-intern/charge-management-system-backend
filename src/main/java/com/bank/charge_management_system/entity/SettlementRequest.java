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
@Table(name = "settlement_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SettlementRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "settlement_id", unique = true, nullable = false, length = 50)
    private String settlementId;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    // Settlement period
    @Column(name = "period_from", nullable = false)
    private LocalDate periodFrom;
    
    @Column(name = "period_to", nullable = false)
    private LocalDate periodTo;
    
    // Financial details
    @Column(name = "total_charges", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCharges;
    
    @Column(name = "currency_code", length = 3)
    private String currencyCode = "INR";
    
    @Column(name = "number_of_transactions", nullable = false)
    private Integer numberOfTransactions;
    
    // Settlement instructions
    @Column(name = "settlement_account", nullable = false, length = 50)
    private String settlementAccount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_method", nullable = false)
    private SettlementMethod settlementMethod = SettlementMethod.DEBIT;
    
    // Status tracking
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    // Audit fields
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User createdByUser;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", insertable = false, updatable = false)
    private User approvedByUser;
    
    // Enums
    public enum SettlementMethod {
        DEBIT, INVOICE, ADJUSTMENT
    }
    
    public enum Status {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    }
}