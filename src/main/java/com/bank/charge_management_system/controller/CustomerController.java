package com.bank.charge_management_system.controller;

import com.bank.charge_management_system.dto.ApiResponse;
import com.bank.charge_management_system.entity.Customer;
import com.bank.charge_management_system.repository.CustomerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer Management", description = "Customer operations")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Get all customers
     */
    @GetMapping
    @Operation(summary = "Get all customers", description = "Retrieve all customers from the system")
    @PreAuthorize("hasAnyRole('ADMIN', 'RULE_CREATOR', 'RULE_APPROVER', 'VIEWER')")
    public ResponseEntity<ApiResponse<List<Customer>>> getAllCustomers() {
        try {
            List<Customer> customers = customerRepository.findAll();
            return ResponseEntity.ok(ApiResponse.success("Customers fetched successfully", customers));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                ApiResponse.error("Failed to fetch customers: " + e.getMessage())
            );
        }
    }

    /**
     * Get all active customers
     */
    @GetMapping("/active")
    @Operation(summary = "Get active customers", description = "Retrieve all active customers")
    @PreAuthorize("hasAnyRole('ADMIN', 'RULE_CREATOR', 'RULE_APPROVER', 'VIEWER')")
    public ResponseEntity<ApiResponse<List<Customer>>> getActiveCustomers() {
        try {
            List<Customer> customers = customerRepository.findByStatus(Customer.Status.ACTIVE);
            return ResponseEntity.ok(ApiResponse.success("Active customers fetched successfully", customers));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                ApiResponse.error("Failed to fetch active customers: " + e.getMessage())
            );
        }
    }

    /**
     * Get customer by customer code
     */
    @GetMapping("/{customerCode}")
    @Operation(summary = "Get customer by code", description = "Retrieve a customer by their customer code")
    @PreAuthorize("hasAnyRole('ADMIN', 'RULE_CREATOR', 'RULE_APPROVER', 'VIEWER')")
    public ResponseEntity<ApiResponse<Customer>> getCustomerByCode(@PathVariable String customerCode) {
        try {
            Optional<Customer> customer = customerRepository.findByCustomerCode(customerCode);
            if (customer.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Customer found", customer.get()));
            } else {
                return ResponseEntity.status(404).body(
                    ApiResponse.error("Customer not found with code: " + customerCode)
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                ApiResponse.error("Failed to fetch customer: " + e.getMessage())
            );
        }
    }

    /**
     * Get customers by type
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Get customers by type", description = "Retrieve customers by type (RETAIL/CORPORATE)")
    @PreAuthorize("hasAnyRole('ADMIN', 'RULE_CREATOR', 'RULE_APPROVER', 'VIEWER')")
    public ResponseEntity<ApiResponse<List<Customer>>> getCustomersByType(@PathVariable String type) {
        try {
            Customer.CustomerType customerType = Customer.CustomerType.valueOf(type.toUpperCase());
            List<Customer> customers = customerRepository.findByCustomerType(customerType);
            return ResponseEntity.ok(ApiResponse.success("Customers fetched successfully", customers));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(
                ApiResponse.error("Invalid customer type. Use RETAIL or CORPORATE")
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                ApiResponse.error("Failed to fetch customers: " + e.getMessage())
            );
        }
    }

    /**
     * Search customers by name
     */
    @GetMapping("/search")
    @Operation(summary = "Search customers", description = "Search customers by name, company name, or customer code")
    @PreAuthorize("hasAnyRole('ADMIN', 'RULE_CREATOR', 'RULE_APPROVER', 'VIEWER')")
    public ResponseEntity<ApiResponse<List<Customer>>> searchCustomers(@RequestParam String query) {
        try {
            List<Customer> customers = customerRepository.searchCustomers(query);
            return ResponseEntity.ok(ApiResponse.success("Search completed", customers));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                ApiResponse.error("Failed to search customers: " + e.getMessage())
            );
        }
    }
}
