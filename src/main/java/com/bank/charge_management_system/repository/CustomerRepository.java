package com.bank.charge_management_system.repository;

import com.bank.charge_management_system.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    /**
     * Find customer by customer code
     */
    Optional<Customer> findByCustomerCode(String customerCode);
    
    /**
     * Find customers by type
     */
    List<Customer> findByCustomerType(Customer.CustomerType customerType);
    
    /**
     * Find customers by status
     */
    List<Customer> findByStatus(Customer.Status status);
    
    /**
     * Find active customers ordered by creation date
     */
    List<Customer> findByStatusOrderByCreatedAtDesc(Customer.Status status);
    
    /**
     * Search customers by name or company name
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.customerCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Customer> searchCustomers(@Param("searchTerm") String searchTerm);
    
    /**
     * Check if customer code exists
     */
    boolean existsByCustomerCode(String customerCode);
    
    /**
     * Count customers by type
     */
    long countByCustomerType(Customer.CustomerType customerType);
    
    /**
     * Find customers by email
     */
    Optional<Customer> findByEmail(String email);
    
    /**
     * Find customers by phone
     */
    Optional<Customer> findByPhone(String phone);
    
    /**
     * Find customers registered in a date range
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "c.registrationDate >= :startDate AND " +
           "c.registrationDate <= :endDate " +
           "ORDER BY c.registrationDate DESC")
    List<Customer> findCustomersRegisteredBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * Find active retail customers
     */
    List<Customer> findByCustomerTypeAndStatus(
        Customer.CustomerType customerType, 
        Customer.Status status
    );
    
    /**
     * Get customers registered this month
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "MONTH(c.registrationDate) = MONTH(CURRENT_DATE) AND " +
           "YEAR(c.registrationDate) = YEAR(CURRENT_DATE) " +
           "ORDER BY c.registrationDate DESC")
    List<Customer> findCustomersRegisteredThisMonth();
    
    /**
     * Count active customers by type
     */
    long countByCustomerTypeAndStatus(Customer.CustomerType customerType, Customer.Status status);
    
    /**
     * Find customers with active status and recent activity
     */
    @Query("SELECT DISTINCT c FROM Customer c " +
           "JOIN Transaction t ON c.id = t.customerId " +
           "WHERE c.status = 'ACTIVE' AND " +
           "t.transactionDate >= :sinceDate " +
           "ORDER BY c.customerCode")
    List<Customer> findActiveCustomersWithRecentActivity(@Param("sinceDate") LocalDateTime sinceDate);
    
    /**
     * Get customer statistics
     */
    @Query("SELECT c.customerType, c.status, COUNT(c) FROM Customer c " +
           "GROUP BY c.customerType, c.status")
    List<Object[]> getCustomerStatistics();
    
    /**
     * Find customers by partial name match (for autocomplete)
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "c.status = 'ACTIVE' AND (" +
           "LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :name, '%'))" +
           ") ORDER BY c.customerCode")
    List<Customer> findActiveCustomersByName(@Param("name") String name);
    
    /**
     * Check if customer has any transactions
     */
    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.customerId = :customerId")
    boolean hasTransactions(@Param("customerId") Long customerId);
}
