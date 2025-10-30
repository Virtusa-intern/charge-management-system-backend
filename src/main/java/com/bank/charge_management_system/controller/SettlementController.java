// src/main/java/com/bank/charge_management_system/controller/SettlementController.java
package com.bank.charge_management_system.controller;

import com.bank.charge_management_system.dto.ApiResponse;
import com.bank.charge_management_system.entity.Customer;
import com.bank.charge_management_system.entity.SettlementRequest;
import com.bank.charge_management_system.repository.CustomerRepository;
import com.bank.charge_management_system.repository.SettlementRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settlements")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"})
public class SettlementController {
    
    @Autowired
    private SettlementRequestRepository settlementRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    /**
     * Get all settlements
     * GET /api/settlements
     * Accessible by: RULE_CREATOR, RULE_APPROVER, VIEWER
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('RULE_CREATOR', 'RULE_APPROVER', 'VIEWER')")
    public ResponseEntity<ApiResponse<List<SettlementRequest>>> getAllSettlements() {
        try {
            List<SettlementRequest> settlements = settlementRepository.findAll();
            return ResponseEntity.ok(ApiResponse.success("Settlements retrieved successfully", settlements));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve settlements: " + e.getMessage(), 500));
        }
    }
    
    /**
     * Get settlement by ID
     * GET /api/settlements/{id}
     * Accessible by: RULE_CREATOR, RULE_APPROVER, VIEWER
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RULE_CREATOR', 'RULE_APPROVER', 'VIEWER')")
    public ResponseEntity<ApiResponse<SettlementRequest>> getSettlementById(@PathVariable Long id) {
        try {
            SettlementRequest settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found with id: " + id));
            
            return ResponseEntity.ok(ApiResponse.success("Settlement retrieved successfully", settlement));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage(), 404));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve settlement: " + e.getMessage(), 500));
        }
    }
    
    /**
     * Get settlements by customer code
     * GET /api/settlements/customer/{customerCode}
     * Accessible by: RULE_CREATOR, RULE_APPROVER, VIEWER
     */
    @GetMapping("/customer/{customerCode}")
    @PreAuthorize("hasAnyRole('RULE_CREATOR', 'RULE_APPROVER', 'VIEWER')")
    public ResponseEntity<ApiResponse<List<SettlementRequest>>> getSettlementsByCustomer(
            @PathVariable String customerCode) {
        try {
            Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerCode));
            
            List<SettlementRequest> settlements = settlementRepository.findByCustomerId(customer.getId());
            return ResponseEntity.ok(ApiResponse.success("Settlements retrieved successfully", settlements));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage(), 404));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve settlements: " + e.getMessage(), 500));
        }
    }
    
    /**
     * Get settlements by status
     * GET /api/settlements/status/{status}
     * Accessible by: RULE_CREATOR, RULE_APPROVER, VIEWER
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('RULE_CREATOR', 'RULE_APPROVER', 'VIEWER')")
    public ResponseEntity<ApiResponse<List<SettlementRequest>>> getSettlementsByStatus(
            @PathVariable String status) {
        try {
            SettlementRequest.Status settlementStatus = SettlementRequest.Status.valueOf(status.toUpperCase());
            List<SettlementRequest> settlements = settlementRepository.findByStatus(settlementStatus);
            
            return ResponseEntity.ok(ApiResponse.success("Settlements retrieved successfully", settlements));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid status: " + status, 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve settlements: " + e.getMessage(), 500));
        }
    }
    
    /**
     * Create new settlement request
     * POST /api/settlements
     * Accessible by: RULE_CREATOR, RULE_APPROVER only
     * ADMIN is DENIED - separation of duties (admins manage system, not financial operations)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('RULE_CREATOR', 'RULE_APPROVER')")
    public ResponseEntity<ApiResponse<SettlementRequest>> createSettlement(
            @RequestBody Map<String, Object> request) {
        try {
            // Extract and validate customer
            String customerCode = (String) request.get("customerCode");
            Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerCode));
            
            // Extract settlement details
            String settlementAccount = (String) request.get("accountNumber");
            String description = (String) request.get("description");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            
            // Determine settlement type from request
            String settlementTypeStr = request.getOrDefault("settlementType", "DEBIT").toString();
            SettlementRequest.SettlementMethod method = 
                "CREDIT".equals(settlementTypeStr) 
                    ? SettlementRequest.SettlementMethod.ADJUSTMENT 
                    : SettlementRequest.SettlementMethod.DEBIT;
            
            // Create settlement request
            SettlementRequest settlement = new SettlementRequest();
            settlement.setSettlementId("SETL_" + System.currentTimeMillis());
            settlement.setCustomerId(customer.getId());
            settlement.setPeriodFrom(LocalDate.now().withDayOfMonth(1)); // Current month start
            settlement.setPeriodTo(LocalDate.now()); // Today
            settlement.setTotalCharges(amount);
            settlement.setNumberOfTransactions(1); // Will be calculated properly in full implementation
            settlement.setSettlementAccount(settlementAccount);
            settlement.setSettlementMethod(method);
            settlement.setStatus(SettlementRequest.Status.PENDING);
            settlement.setCreatedBy(1L); // TODO: Get from security context
            
            SettlementRequest savedSettlement = settlementRepository.save(settlement);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Settlement created successfully", savedSettlement));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create settlement: " + e.getMessage(), 500));
        }
    }
    
    /**
     * Approve settlement request
     * POST /api/settlements/{id}/approve
     * Accessible by: RULE_APPROVER only (requires approval authority)
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('RULE_APPROVER')")
    public ResponseEntity<ApiResponse<SettlementRequest>> approveSettlement(@PathVariable Long id) {
        try {
            SettlementRequest settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found with id: " + id));
            
            if (settlement.getStatus() != SettlementRequest.Status.PENDING) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Only pending settlements can be approved", 400));
            }
            
            settlement.setStatus(SettlementRequest.Status.PROCESSING);
            settlement.setApprovedBy(1L); // TODO: Get from security context
            settlement.setApprovedAt(LocalDateTime.now());
            
            SettlementRequest savedSettlement = settlementRepository.save(settlement);
            
            return ResponseEntity.ok(ApiResponse.success("Settlement approved successfully", savedSettlement));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage(), 404));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to approve settlement: " + e.getMessage(), 500));
        }
    }
    
    /**
     * Cancel settlement request
     * POST /api/settlements/{id}/cancel
     * Accessible by: RULE_CREATOR, RULE_APPROVER
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('RULE_CREATOR', 'RULE_APPROVER')")
    public ResponseEntity<ApiResponse<SettlementRequest>> cancelSettlement(@PathVariable Long id) {
        try {
            SettlementRequest settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found with id: " + id));
            
            if (settlement.getStatus() == SettlementRequest.Status.COMPLETED) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Cannot cancel completed settlements", 400));
            }
            
            settlement.setStatus(SettlementRequest.Status.CANCELLED);
            SettlementRequest savedSettlement = settlementRepository.save(settlement);
            
            return ResponseEntity.ok(ApiResponse.success("Settlement cancelled successfully", savedSettlement));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage(), 404));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to cancel settlement: " + e.getMessage(), 500));
        }
    }
    
    /**
     * Get settlement statistics
     * GET /api/settlements/statistics
     * Accessible by: RULE_CREATOR, RULE_APPROVER, VIEWER
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('RULE_CREATOR', 'RULE_APPROVER', 'VIEWER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSettlementStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            statistics.put("totalSettlements", settlementRepository.count());
            statistics.put("pendingSettlements", settlementRepository.countByStatus(SettlementRequest.Status.PENDING));
            statistics.put("processingSettlements", settlementRepository.countByStatus(SettlementRequest.Status.PROCESSING));
            statistics.put("completedSettlements", settlementRepository.countByStatus(SettlementRequest.Status.COMPLETED));
            statistics.put("failedSettlements", settlementRepository.countByStatus(SettlementRequest.Status.FAILED));
            statistics.put("cancelledSettlements", settlementRepository.countByStatus(SettlementRequest.Status.CANCELLED));
            
            return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", statistics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve statistics: " + e.getMessage(), 500));
        }
    }
}