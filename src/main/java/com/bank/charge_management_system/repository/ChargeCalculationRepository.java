package com.bank.charge_management_system.repository;

import com.bank.charge_management_system.entity.ChargeCalculation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ChargeCalculationRepository extends JpaRepository<ChargeCalculation, Long> {
    
    /**
     * Find charge calculations by transaction
     */
    List<ChargeCalculation> findByTransactionId(Long transactionId);
    
    /**
     * Find charge calculations by rule
     */
    List<ChargeCalculation> findByRuleId(Long ruleId);
    
    /**
     * Find charge calculations by status
     */
    List<ChargeCalculation> findByStatus(ChargeCalculation.Status status);
    
    /**
     * Check if a charge already exists for customer, rule and period
     */
    @Query("SELECT COUNT(cc) > 0 FROM ChargeCalculation cc " +
           "JOIN Transaction t ON cc.transactionId = t.id " +
           "WHERE t.customerId = :customerId AND " +
           "cc.ruleId = :ruleId AND " +
           "cc.periodStart >= :periodStart AND " +
           "cc.periodEnd <= :periodEnd")
    boolean existsByCustomerAndRuleAndPeriod(
        @Param("customerId") Long customerId,
        @Param("ruleId") Long ruleId,
        @Param("periodStart") LocalDate periodStart,
        @Param("periodEnd") LocalDate periodEnd
    );
    
    /**
     * Get total charges for a customer in a period
     */
    @Query("SELECT SUM(cc.calculatedAmount) FROM ChargeCalculation cc " +
           "JOIN Transaction t ON cc.transactionId = t.id " +
           "WHERE t.customerId = :customerId AND " +
           "cc.periodStart >= :periodStart AND " +
           "cc.periodEnd <= :periodEnd AND " +
           "cc.status = 'CALCULATED'")
    java.math.BigDecimal getTotalChargesForCustomerInPeriod(
        @Param("customerId") Long customerId,
        @Param("periodStart") LocalDate periodStart,
        @Param("periodEnd") LocalDate periodEnd
    );
    
    /**
     * Find charge calculations by customer and period
     */
    @Query("SELECT cc FROM ChargeCalculation cc " +
           "JOIN Transaction t ON cc.transactionId = t.id " +
           "WHERE t.customerId = :customerId AND " +
           "cc.periodStart >= :periodStart AND " +
           "cc.periodEnd <= :periodEnd " +
           "ORDER BY cc.createdAt DESC")
    List<ChargeCalculation> findChargesByCustomerAndPeriod(
        @Param("customerId") Long customerId,
        @Param("periodStart") LocalDate periodStart,
        @Param("periodEnd") LocalDate periodEnd
    );
    
    /**
     * Get charge calculations pending for settlement
     */
    @Query("SELECT cc FROM ChargeCalculation cc WHERE " +
           "cc.status = 'CALCULATED' AND " +
           "cc.appliedAt IS NULL")
    List<ChargeCalculation> findPendingChargesForSettlement();

    @Query("SELECT COUNT(cc) > 0 FROM ChargeCalculation cc " +
       "JOIN Transaction t ON cc.transactionId = t.id " +
       "WHERE t.customerId = :customerId AND " +
       "cc.ruleId = :ruleId AND " +
       "MONTH(cc.createdAt) = MONTH(CURRENT_DATE) AND " +
       "YEAR(cc.createdAt) = YEAR(CURRENT_DATE) AND " +
       "cc.status != 'REVERSED'")
    boolean existsMonthlyChargeForCustomerAndRule(
        @Param("customerId") Long customerId,
        @Param("ruleId") Long ruleId
    );

    @Query("SELECT COUNT(cc) > 0 FROM ChargeCalculation cc " +
           "JOIN Transaction t ON cc.transactionId = t.id " +
           "WHERE t.customerId = :customerId AND " +
           "cc.ruleId = :ruleId AND " +
           "cc.createdAt >= :startDate AND " +
           "cc.status != 'REVERSED'")
    boolean existsBiMonthlyChargeForCustomerAndRule(
        @Param("customerId") Long customerId,
        @Param("ruleId") Long ruleId,
        @Param("startDate") LocalDate startDate
    );
}