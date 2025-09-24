package com.bank.charge_management_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_id", unique = true, nullable = false, length = 50)
    private String transactionId;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "currency_code", length = 3)
    private String currencyCode = "INR";
    
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate = LocalDateTime.now();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;
    
    @Column(name = "source_account", length = 50)
    private String sourceAccount;
    
    @Column(name = "destination_account", length = 50)
    private String destinationAccount;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_json")
    private Map<String, Object> metadata;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Relationship to Customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;
    
    public enum Channel {
        ATM, ONLINE, BRANCH, MOBILE, API
    }
    
    public enum Status {
        PENDING, PROCESSED, FAILED
    }
}