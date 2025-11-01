// src/main/java/com/bank/chargemgmt/controller/ChargeCalculationController.java
package com.bank.charge_management_system.controller;

import com.bank.charge_management_system.dto.*;
import com.bank.charge_management_system.entity.ChargeCalculation;
import com.bank.charge_management_system.entity.Transaction;
import com.bank.charge_management_system.repository.ChargeCalculationRepository;
import com.bank.charge_management_system.repository.TransactionRepository;
import com.bank.charge_management_system.service.ChargeCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/charges")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:8081" })
@PreAuthorize("hasAnyRole('ADMIN', 'RULE_CREATOR', 'RULE_APPROVER', 'VIEWER')")
public class ChargeCalculationController {

    @Autowired
    private ChargeCalculationService chargeCalculationService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ChargeCalculationRepository chargeCalculationRepository;

    /**
     * Calculate charges for a single transaction
     * POST /api/charges/calculate
     */
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<ChargeCalculationResult>> calculateCharges(
            @Valid @RequestBody TransactionRequest request,
            BindingResult bindingResult) {

        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .collect(Collectors.toList());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Validation failed: " + String.join(", ", errors), 400));
            }

            ChargeCalculationResult result = chargeCalculationService.calculateChargesForTransaction(request);

            if (result.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.success("Charges calculated successfully", result));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(result.getMessage(), 400));
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid request: " + e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Charge calculation failed: " + e.getMessage(), 500));
        }
    }

    /**
     * Test charge calculation with sample data
     * POST /api/charges/test
     */
    @PostMapping("/test")
    public ResponseEntity<ApiResponse<ChargeTestResult>> testChargeCalculation(
            @Valid @RequestBody ChargeTestRequest request,
            BindingResult bindingResult) {

        try {
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .collect(Collectors.toList());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Validation failed: " + String.join(", ", errors), 400));
            }

            ChargeTestResult testResult = chargeCalculationService.testChargeCalculation(request);

            if (testResult.isTestSuccessful()) {
                return ResponseEntity.ok(ApiResponse.success("Charge test completed successfully", testResult));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(testResult.getTestSummary(), 400));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Charge test failed: " + e.getMessage(), 500));
        }
    }

    /**
     * Calculate charges for multiple transactions (bulk processing)
     * POST /api/charges/bulk-calculate
     */
    @PostMapping("/bulk-calculate")
    public ResponseEntity<ApiResponse<BulkChargeCalculationResult>> bulkCalculateCharges(
            @Valid @RequestBody BulkChargeCalculationRequest request,
            BindingResult bindingResult) {

        try {
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .collect(Collectors.toList());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Validation failed: " + String.join(", ", errors), 400));
            }

            BulkChargeCalculationResult bulkResult = chargeCalculationService.processBulkCalculations(request);

            return ResponseEntity.ok(ApiResponse.success("Bulk charge calculation completed", bulkResult));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Bulk charge calculation failed: " + e.getMessage(), 500));
        }
    }

    /**
     * Get predefined test scenarios
     * GET /api/charges/test-scenarios
     */
    @GetMapping("/test-scenarios")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTestScenarios() {
        try {
            Map<String, Object> scenarios = chargeCalculationService.getTestScenarios();
            return ResponseEntity.ok(ApiResponse.success("Test scenarios retrieved successfully", scenarios));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve test scenarios: " + e.getMessage(), 500));
        }
    }

    /**
     * Quick test with sample customer and transaction
     * GET /api/charges/quick-test
     */
    @GetMapping("/quick-test")
    public ResponseEntity<ApiResponse<ChargeCalculationResult>> quickTest(
            @RequestParam(defaultValue = "CUST001") String customerCode,
            @RequestParam(defaultValue = "ATM_WITHDRAWAL_PARENT") String transactionType,
            @RequestParam(defaultValue = "1000") String amount) {

        try {
            // Create a quick test transaction
            TransactionRequest quickRequest = new TransactionRequest();
            quickRequest.setTransactionId("QUICK_TEST_" + System.currentTimeMillis());
            quickRequest.setCustomerCode(customerCode);
            quickRequest.setTransactionType(transactionType);
            quickRequest.setAmount(new BigDecimal(amount));
            quickRequest.setChannel("ATM");
            quickRequest.setTransactionDate(LocalDateTime.now());

            ChargeCalculationResult result = chargeCalculationService.calculateChargesForTransaction(quickRequest);

            return ResponseEntity.ok(ApiResponse.success("Quick test completed", result));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Quick test failed: " + e.getMessage(), 500));
        }
    }

    /**
     * Simulate multiple transactions for comprehensive testing
     * POST /api/charges/simulate
     */
    @PostMapping("/simulate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> simulateTransactions(
            @RequestParam(defaultValue = "CUST001") String customerCode,
            @RequestParam(defaultValue = "5") int transactionCount) {

        try {
            Map<String, Object> simulationResult = new HashMap<>();
            List<ChargeCalculationResult> results = new ArrayList<>();
            BigDecimal totalCharges = BigDecimal.ZERO;

            // Define simulation scenarios
            String[] transactionTypes = { "ATM_WITHDRAWAL_PARENT", "FUNDS_TRANSFER", "ATM_WITHDRAWAL_OTHER",
                    "STATEMENT_PRINT" };
            String[] channels = { "ATM", "ONLINE", "BRANCH", "MOBILE" };
            BigDecimal[] amounts = { new BigDecimal("500"), new BigDecimal("1000"), new BigDecimal("2000"),
                    new BigDecimal("5000"), new BigDecimal("10000") };

            Random random = new Random();

            for (int i = 0; i < transactionCount; i++) {
                // Create varied transaction requests
                TransactionRequest request = new TransactionRequest();
                request.setTransactionId("SIM_" + customerCode + "_" + System.currentTimeMillis() + "_" + (i + 1));
                request.setCustomerCode(customerCode);
                request.setTransactionType(transactionTypes[random.nextInt(transactionTypes.length)]);
                request.setAmount(amounts[random.nextInt(amounts.length)]);
                request.setChannel(channels[random.nextInt(channels.length)]);
                request.setTransactionDate(LocalDateTime.now().minusMinutes(random.nextInt(1440))); // Random time in
                                                                                                    // last 24 hours

                ChargeCalculationResult result = chargeCalculationService.calculateChargesForTransaction(request);
                results.add(result);

                if (result.isSuccess()) {
                    totalCharges = totalCharges.add(result.getTotalCharges());
                }
            }

            // Build simulation summary
            simulationResult.put("customerCode", customerCode);
            simulationResult.put("transactionsSimulated", transactionCount);
            simulationResult.put("totalCharges", totalCharges);
            simulationResult.put("results", results);

            // Calculate statistics
            long successfulCalculations = results.stream().filter(ChargeCalculationResult::isSuccess).count();
            simulationResult.put("successfulCalculations", successfulCalculations);
            simulationResult.put("failedCalculations", transactionCount - successfulCalculations);

            Map<String, Long> transactionTypeCount = results.stream()
                    .collect(Collectors.groupingBy(ChargeCalculationResult::getTransactionType, Collectors.counting()));
            simulationResult.put("transactionTypeBreakdown", transactionTypeCount);

            return ResponseEntity.ok(ApiResponse.success("Simulation completed successfully", simulationResult));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Simulation failed: " + e.getMessage(), 500));
        }
    }

    /**
     * Validate a transaction request without calculating charges
     * POST /api/charges/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateTransaction(
            @Valid @RequestBody TransactionRequest request,
            BindingResult bindingResult) {

        try {
            Map<String, Object> validationResult = new HashMap<>();
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            // Check binding result errors
            if (bindingResult.hasErrors()) {
                errors.addAll(bindingResult.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .collect(Collectors.toList()));
            }

            // Additional business validation
            try {
                // This will throw exception if customer not found
                chargeCalculationService.calculateChargesForTransaction(request);
                validationResult.put("customerExists", true);
            } catch (IllegalArgumentException e) {
                if (e.getMessage().contains("Customer not found")) {
                    errors.add("Customer not found: " + request.getCustomerCode());
                    validationResult.put("customerExists", false);
                } else if (e.getMessage().contains("Transaction ID already exists")) {
                    warnings.add("Transaction ID already exists - please use unique ID");
                } else {
                    errors.add(e.getMessage());
                }
            }

            // Validate amount ranges
            if (request.getAmount().compareTo(new BigDecimal("10000000")) > 0) {
                warnings.add("Very large transaction amount - please verify");
            }

            if (request.getAmount().compareTo(new BigDecimal("0.01")) < 0) {
                errors.add("Transaction amount too small");
            }

            validationResult.put("valid", errors.isEmpty());
            validationResult.put("errors", errors);
            validationResult.put("warnings", warnings);
            validationResult.put("errorCount", errors.size());
            validationResult.put("warningCount", warnings.size());

            if (errors.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Transaction validation passed", validationResult));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Transaction validation failed", validationResult));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Validation failed: " + e.getMessage(), 500));
        }
    }

    /**
     * Get charge calculation statistics and summary
     * GET /api/charges/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChargeStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // Get real statistics from database
            long totalTransactions = transactionRepository.count();
            long totalChargeCalculations = chargeCalculationRepository.count();

            // Get today's transactions
            LocalDateTime startOfDay = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
            long transactionsToday = transactionRepository.countByCreatedAtAfter(startOfDay);

            // Get total charges amount
            BigDecimal totalCharges = chargeCalculationRepository.findAll().stream()
                    .map(ChargeCalculation::getCalculatedAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Get average charge
            BigDecimal averageCharge = totalChargeCalculations > 0
                    ? totalCharges.divide(BigDecimal.valueOf(totalChargeCalculations), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // Get most used rules
            List<Object[]> topRules = chargeCalculationRepository.findTopRulesByUsage();
            List<Map<String, Object>> mostUsedRules = topRules.stream()
                    .limit(5)
                    .map(result -> {
                        Map<String, Object> rule = new HashMap<>();
                        rule.put("ruleCode", result[0]);
                        rule.put("ruleName", result[1]);
                        rule.put("usageCount", result[2]);
                        return rule;
                    })
                    .collect(Collectors.toList());

            // Get transaction type distribution
            Map<String, Long> transactionTypes = transactionRepository.findAll().stream()
                    .collect(Collectors.groupingBy(Transaction::getTransactionType, Collectors.counting()));

            // Get this month's statistics
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
            long transactionsThisMonth = transactionRepository.countByCreatedAtAfter(startOfMonth);

            BigDecimal chargesThisMonth = chargeCalculationRepository.findAll().stream()
                    .filter(c -> c.getCreatedAt().isAfter(startOfMonth))
                    .map(ChargeCalculation::getCalculatedAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // ✅ NEW: Get recent transactions and calculations for Reports tab
            List<Transaction> allTransactions = transactionRepository.findAll();
            List<ChargeCalculation> allCalculations = chargeCalculationRepository.findAll();

            // Convert to maps for frontend with customer information
            List<Map<String, Object>> recentTransactions = allTransactions.stream()
                    .map(tx -> {
                        Map<String, Object> txMap = new HashMap<>();
                        txMap.put("id", tx.getId());
                        txMap.put("transactionId", tx.getTransactionId());
                        txMap.put("customerId", tx.getCustomerId());

                        // ✅ Get customer information if available
                        if (tx.getCustomer() != null) {
                            txMap.put("customerCode", tx.getCustomer().getCustomerCode());
                        } else {
                            txMap.put("customerCode", "UNKNOWN-" + tx.getCustomerId());
                        }

                        txMap.put("transactionType", tx.getTransactionType());
                        txMap.put("amount", tx.getAmount());
                        txMap.put("channel", tx.getChannel());
                        txMap.put("status", tx.getStatus());
                        txMap.put("transactionDate", tx.getTransactionDate());
                        txMap.put("createdAt", tx.getCreatedAt());
                        return txMap;
                    })
                    .collect(Collectors.toList());

            List<Map<String, Object>> recentCalculations = allCalculations.stream()
                    .map(calc -> {
                        Map<String, Object> calcMap = new HashMap<>();
                        calcMap.put("id", calc.getId());
                        calcMap.put("transactionId", calc.getTransactionId());
                        calcMap.put("ruleId", calc.getRuleId());
                        calcMap.put("calculatedAmount", calc.getCalculatedAmount());
                        calcMap.put("status", calc.getStatus());
                        calcMap.put("createdAt", calc.getCreatedAt());
                        return calcMap;
                    })
                    .collect(Collectors.toList());

            statistics.put("totalTransactions", totalTransactions);
            statistics.put("totalChargeCalculations", totalChargeCalculations);
            statistics.put("transactionsToday", transactionsToday);
            statistics.put("transactionsThisMonth", transactionsThisMonth);
            statistics.put("totalChargesCollected", totalCharges);
            statistics.put("chargesThisMonth", chargesThisMonth);
            statistics.put("averageChargePerTransaction", averageCharge);
            statistics.put("mostUsedRules", mostUsedRules);
            statistics.put("transactionTypeDistribution", transactionTypes);
            statistics.put("recentTransactions", recentTransactions); // ✅ NEW
            statistics.put("recentCalculations", recentCalculations); // ✅ NEW
            statistics.put("systemStatus", "OPERATIONAL");
            statistics.put("lastUpdated", LocalDateTime.now());

            return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", statistics));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve statistics: " + e.getMessage(), 500));
        }
    }

    /**
     * Health check for charge calculation service
     * GET /api/charges/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        try {
            Map<String, Object> healthStatus = new HashMap<>();

            // Test basic service functionality
            Map<String, Object> testScenarios = chargeCalculationService.getTestScenarios();

            healthStatus.put("status", "HEALTHY");
            healthStatus.put("service", "Charge Calculation Service");
            healthStatus.put("timestamp", LocalDateTime.now());
            healthStatus.put("testScenariosAvailable", testScenarios.size());
            healthStatus.put("version", "1.0.0");

            // Test database connectivity implicitly through scenarios
            healthStatus.put("databaseConnectivity", "OK");
            healthStatus.put("rulesEngineStatus", "OPERATIONAL");

            return ResponseEntity.ok(ApiResponse.success("Charge calculation service is healthy", healthStatus));

        } catch (Exception e) {
            Map<String, Object> healthStatus = new HashMap<>();
            healthStatus.put("status", "UNHEALTHY");
            healthStatus.put("error", e.getMessage());
            healthStatus.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("Charge calculation service is unhealthy", healthStatus));
        }
    }

    @GetMapping("/history/{customerCode}")
    public ResponseEntity<ApiResponse<List<TransactionHistoryDto>>> getTransactionHistory(
            @PathVariable String customerCode) {
        try {
            List<TransactionHistoryDto> history = chargeCalculationService.getTransactionHistory(customerCode);
            return ResponseEntity.ok(ApiResponse.success("Transaction history retrieved successfully", history));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve transaction history: " + e.getMessage(), 500));
        }
    }

    /**
     * Get paginated and filtered transaction history
     */
    @GetMapping("/history/{customerCode}/paged")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionHistoryDto>>> getTransactionHistoryPaged(
            @PathVariable String customerCode,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Page<TransactionHistoryDto> historyPage = chargeCalculationService.getTransactionHistoryPaged(
                    customerCode, transactionType, channel, status, startDate, endDate, page, size, sortBy, sortDir);

            PagedResponse<TransactionHistoryDto> response = new PagedResponse<>(
                    historyPage.getContent(),
                    historyPage.getNumber(),
                    historyPage.getSize(),
                    historyPage.getTotalElements(),
                    historyPage.getTotalPages());

            return ResponseEntity.ok(ApiResponse.success("Transaction history retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve transaction history: " + e.getMessage(), 500));
        }
    }

    /**
     * Get sample transaction requests for testing
     * GET /api/charges/sample-requests
     */
    @GetMapping("/sample-requests")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSampleRequests() {
        try {
            Map<String, Object> samples = new HashMap<>();

            // ATM Withdrawal Samples
            List<TransactionRequest> atmSamples = new ArrayList<>();
            atmSamples.add(new TransactionRequest("TXN_ATM_001", "CUST001", "ATM_WITHDRAWAL_PARENT",
                    new BigDecimal("1000"), "ATM"));
            atmSamples.add(new TransactionRequest("TXN_ATM_002", "CUST001", "ATM_WITHDRAWAL_OTHER",
                    new BigDecimal("2000"), "ATM"));

            // Funds Transfer Samples
            List<TransactionRequest> transferSamples = new ArrayList<>();
            transferSamples.add(new TransactionRequest("TXN_FT_001", "CUST001", "FUNDS_TRANSFER",
                    new BigDecimal("5000"), "ONLINE"));
            transferSamples.add(new TransactionRequest("TXN_FT_002", "CUST002", "FUNDS_TRANSFER",
                    new BigDecimal("25000"), "BRANCH"));

            // Special Services Samples
            List<TransactionRequest> specialSamples = new ArrayList<>();
            specialSamples.add(
                    new TransactionRequest("TXN_SP_001", "CUST001", "STATEMENT_PRINT", new BigDecimal("1"), "BRANCH"));
            specialSamples.add(new TransactionRequest("TXN_SP_002", "CUST001", "DUPLICATE_DEBIT_CARD",
                    new BigDecimal("1"), "BRANCH"));

            samples.put("atm_samples", atmSamples);
            samples.put("transfer_samples", transferSamples);
            samples.put("special_samples", specialSamples);

            // Usage instructions
            samples.put("usage", "Copy any sample request and POST to /api/charges/calculate");
            samples.put("note", "Remember to change transaction IDs to unique values");

            return ResponseEntity.ok(ApiResponse.success("Sample requests generated successfully", samples));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate sample requests: " + e.getMessage(), 500));
        }
    }

    /**
     * Export charge calculations within a date range (CSV payload)
     * GET /api/charges/export?start=2025-10-01T00:00:00&end=2025-10-31T23:59:59
     */
    @GetMapping("/export")
    public ResponseEntity<List<Map<String, Object>>> exportCharges(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            LocalDateTime effectiveStart = start != null ? start : LocalDateTime.now().minusMonths(1);
            LocalDateTime effectiveEnd = end != null ? end : LocalDateTime.now();

            List<ChargeCalculation> charges = chargeCalculationRepository.findByCreatedAtBetween(effectiveStart,
                    effectiveEnd);

            // Map to export-friendly structure
            List<Map<String, Object>> export = charges.stream().map(cc -> {
                Map<String, Object> row = new HashMap<>();
                row.put("chargeId", cc.getId());
                row.put("transactionId", cc.getTransactionId());
                row.put("ruleId", cc.getRuleId());
                row.put("calculatedAmount", cc.getCalculatedAmount());
                row.put("currency", cc.getCurrencyCode());
                row.put("calculationBasis", cc.getCalculationBasis());
                row.put("periodStart", cc.getPeriodStart());
                row.put("periodEnd", cc.getPeriodEnd());
                row.put("status", cc.getStatus());
                row.put("appliedAt", cc.getAppliedAt());
                row.put("createdAt", cc.getCreatedAt());
                // Include some transaction/customer info if available
                if (cc.getTransaction() != null) {
                    row.put("transactionType", cc.getTransaction().getTransactionType());
                    row.put("transactionAmount", cc.getTransaction().getAmount());
                    row.put("customerId", cc.getTransaction().getCustomerId());
                    if (cc.getTransaction().getCustomer() != null) {
                        row.put("customerCode", cc.getTransaction().getCustomer().getCustomerCode());
                        String customerName = "";
                        if (cc.getTransaction().getCustomer().getCompanyName() != null
                                && !cc.getTransaction().getCustomer().getCompanyName().isEmpty()) {
                            customerName = cc.getTransaction().getCustomer().getCompanyName();
                        } else {
                            String fn = cc.getTransaction().getCustomer().getFirstName();
                            String ln = cc.getTransaction().getCustomer().getLastName();
                            customerName = (fn != null ? fn : "") + (ln != null ? " " + ln : "");
                        }
                        row.put("customerName", customerName.trim());
                    }
                }
                if (cc.getChargeRule() != null) {
                    row.put("ruleCode", cc.getChargeRule().getRuleCode());
                    row.put("ruleName", cc.getChargeRule().getRuleName());
                }
                return row;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(export);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
}