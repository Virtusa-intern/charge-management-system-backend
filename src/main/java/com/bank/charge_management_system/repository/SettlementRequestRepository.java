package com.bank.charge_management_system.repository;

import com.bank.charge_management_system.entity.SettlementRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementRequestRepository extends JpaRepository<SettlementRequest, Long> {
    
    /**
     * Find settlement by settlement ID
     */
    Optional<SettlementRequest> findBySettlementId(String settlementId);
    
    /**
     * Find settlements by customer
     */
    List<SettlementRequest> findByCustomerId(Long customerId);
    
    /**
     * Find settlements by status
     */
    List<SettlementRequest> findByStatus(SettlementRequest.Status status);
    
    /**
     * Find settlements by customer and status
     */
    List<SettlementRequest> findByCustomerIdAndStatus(Long customerId, SettlementRequest.Status status);
    
    /**
     * Find settlements by period
     */
    @Query("SELECT s FROM SettlementRequest s WHERE " +
           "s.periodFrom >= :periodFrom AND s.periodTo <= :periodTo " +
           "ORDER BY s.createdAt DESC")
    List<SettlementRequest> findByPeriod(
        @Param("periodFrom") LocalDate periodFrom,
        @Param("periodTo") LocalDate periodTo
    );
    
    /**
     * Find settlements created between dates
     */
    @Query("SELECT s FROM SettlementRequest s WHERE " +
           "s.createdAt >= :startDate AND s.createdAt <= :endDate " +
           "ORDER BY s.createdAt DESC")
    List<SettlementRequest> findByCreatedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find pending settlements for a customer
     */
    @Query("SELECT s FROM SettlementRequest s WHERE " +
           "s.customerId = :customerId AND " +
           "s.status = 'PENDING' " +
           "ORDER BY s.createdAt DESC")
    List<SettlementRequest> findPendingSettlementsByCustomer(@Param("customerId") Long customerId);
    
    /**
     * Check if settlement already exists for customer and period
     */
    @Query("SELECT COUNT(s) > 0 FROM SettlementRequest s WHERE " +
           "s.customerId = :customerId AND " +
           "s.periodFrom = :periodFrom AND " +
           "s.periodTo = :periodTo AND " +
           "s.status != 'CANCELLED'")
    boolean existsByCustomerAndPeriod(
        @Param("customerId") Long customerId,
        @Param("periodFrom") LocalDate periodFrom,
        @Param("periodTo") LocalDate periodTo
    );
    
    /**
     * Get total charges for a customer in period
     */
    @Query("SELECT SUM(s.totalCharges) FROM SettlementRequest s WHERE " +
           "s.customerId = :customerId AND " +
           "s.periodFrom >= :periodFrom AND " +
           "s.periodTo <= :periodTo AND " +
           "s.status = 'COMPLETED'")
    java.math.BigDecimal getTotalChargesByCustomerAndPeriod(
        @Param("customerId") Long customerId,
        @Param("periodFrom") LocalDate periodFrom,
        @Param("periodTo") LocalDate periodTo
    );
    
    /**
     * Count settlements by status
     */
    long countByStatus(SettlementRequest.Status status);
    
    /**
     * Find recent settlements
     */
    @Query("SELECT s FROM SettlementRequest s " +
           "ORDER BY s.createdAt DESC")
    List<SettlementRequest> findRecentSettlements();
}