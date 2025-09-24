package com.bank.charge_management_system.repository;

import com.bank.charge_management_system.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    /**
     * Find transaction by transaction ID
     */
    Optional<Transaction> findByTransactionId(String transactionId);
    
    /**
     * Find transactions by customer
     */
    List<Transaction> findByCustomerId(Long customerId);
    
    /**
     * Find transactions by customer and type
     */
    List<Transaction> findByCustomerIdAndTransactionType(Long customerId, String transactionType);
    
    /**
     * Count transactions for a customer within a period
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE " +
           "t.customerId = :customerId AND " +
           "t.transactionType = :transactionType AND " +
           "t.transactionDate >= :startDate AND " +
           "t.transactionDate < :endDate")
    int countTransactionsByCustomerAndTypeAndPeriod(
        @Param("customerId") Long customerId,
        @Param("transactionType") String transactionType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find transactions by customer within date range
     */
    @Query("SELECT t FROM Transaction t WHERE " +
           "t.customerId = :customerId AND " +
           "t.transactionDate >= :startDate AND " +
           "t.transactionDate <= :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findTransactionsByCustomerAndDateRange(
        @Param("customerId") Long customerId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find transactions by status
     */
    List<Transaction> findByStatus(Transaction.Status status);
    
    /**
     * Find transactions by channel
     */
    List<Transaction> findByChannel(Transaction.Channel channel);
    
    /**
     * Get transaction count by type for a customer in current month
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE " +
           "t.customerId = :customerId AND " +
           "t.transactionType = :transactionType AND " +
           "MONTH(t.transactionDate) = MONTH(CURRENT_DATE) AND " +
           "YEAR(t.transactionDate) = YEAR(CURRENT_DATE)")
    int getMonthlyTransactionCountByType(
        @Param("customerId") Long customerId,
        @Param("transactionType") String transactionType
    );

    boolean existsByTransactionId(String transactionId);
}