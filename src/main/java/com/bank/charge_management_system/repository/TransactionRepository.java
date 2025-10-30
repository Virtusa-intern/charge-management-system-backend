package com.bank.charge_management_system.repository;

import com.bank.charge_management_system.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
     * Find transactions by customer ordered by date descending (newest first)
     */
    @Query("SELECT t FROM Transaction t WHERE t.customerId = :customerId ORDER BY t.transactionDate DESC")
    List<Transaction> findByCustomerId(@Param("customerId") Long customerId);

    /**
     * Find transactions by customer with pagination and filtering
     */
    @Query("SELECT t FROM Transaction t WHERE t.customerId = :customerId " +
            "AND (:transactionType IS NULL OR t.transactionType = :transactionType) " +
            "AND (:channel IS NULL OR t.channel = :channel) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
            "AND (:endDate IS NULL OR t.transactionDate <= :endDate)")
    Page<Transaction> findByCustomerIdWithFilters(
            @Param("customerId") Long customerId,
            @Param("transactionType") String transactionType,
            @Param("channel") Transaction.Channel channel,
            @Param("status") Transaction.Status status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

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
            @Param("endDate") LocalDateTime endDate);

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
            @Param("endDate") LocalDateTime endDate);

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
            @Param("transactionType") String transactionType);

    boolean existsByTransactionId(String transactionId);

    /**
     * Count transactions created after a specific date
     */
    long countByCreatedAtAfter(LocalDateTime createdAt);

    /**
     * Get average balance for the last two months (placeholder)
     */
    default BigDecimal getAverageBalanceForLastTwoMonths(@Param("customerId") Long customerId) {
        // This is a placeholder implementation
        return new BigDecimal("150000.00");
    }
}