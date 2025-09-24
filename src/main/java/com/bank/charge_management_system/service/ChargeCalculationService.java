// src/main/java/com/bank/chargemgmt/service/ChargeCalculationService.java
package com.bank.charge_management_system.service;

import com.bank.charge_management_system.dto.*;
import com.bank.charge_management_system.entity.*;
import com.bank.charge_management_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChargeCalculationService {

    @Autowired
    private ChargeRuleRepository chargeRuleRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private ChargeCalculationRepository chargeCalculationRepository;
    
    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Calculate charges for a single transaction
     */
    public ChargeCalculationResult calculateChargesForTransaction(TransactionRequest request) {
        ChargeCalculationResult result = new ChargeCalculationResult();
        result.setTransactionId(request.getTransactionId());
        result.setCustomerCode(request.getCustomerCode());
        result.setTransactionType(request.getTransactionType());
        result.setTransactionAmount(request.getAmount());
        result.setCalculationTimestamp(LocalDateTime.now());
        
        try {
            // Validate transaction request
            validateTransactionRequest(request);
            
            // Get customer details
            Customer customer = customerRepository.findByCustomerCode(request.getCustomerCode())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.getCustomerCode()));
            
            // Determine customer category for rule matching
            ChargeRule.Category customerCategory = customer.getCustomerType() == Customer.CustomerType.RETAIL 
                ? ChargeRule.Category.RETAIL_BANKING 
                : ChargeRule.Category.CORP_BANKING;
            
            // Find applicable rules for this transaction
            List<ChargeRule> applicableRules = findApplicableRules(request, customerCategory);
            
            // Calculate charges for each applicable rule
            for (ChargeRule rule : applicableRules) {
                ChargeCalculationDetail detail = calculateChargeForRule(request, customer, rule);
                if (detail != null && detail.getChargeAmount().compareTo(BigDecimal.ZERO) > 0) {
                    result.addCharge(detail);
                }
            }
            
            result.setSuccess(true);
            result.setMessage("Charges calculated successfully");
            result.generateSummary();
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Charge calculation failed: " + e.getMessage());
            result.setCalculationSummary("Error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Test charge calculation with multiple scenarios
     */
    public ChargeTestResult testChargeCalculation(ChargeTestRequest request) {
        ChargeTestResult testResult = new ChargeTestResult();
        testResult.setCustomerCode(request.getCustomerCode());
        testResult.setTestTimestamp(LocalDateTime.now());
        testResult.setTestDescription(request.getTestDescription());
        
        try {
            // Get customer information
            Customer customer = customerRepository.findByCustomerCode(request.getCustomerCode())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.getCustomerCode()));
            
            testResult.setCustomerName(getCustomerDisplayName(customer));
            testResult.setCustomerType(customer.getCustomerType().toString());
            
            int transactionIndex = 0;
            
            // Process each test transaction
            for (ChargeTestRequest.TestTransaction testTx : request.getTestTransactions()) {
                transactionIndex++;
                String testTransactionId = "TEST_" + request.getCustomerCode() + "_" + System.currentTimeMillis() + "_" + transactionIndex;
                
                // Create transaction request
                TransactionRequest txRequest = new TransactionRequest();
                txRequest.setTransactionId(testTransactionId);
                txRequest.setCustomerCode(request.getCustomerCode());
                txRequest.setTransactionType(testTx.getTransactionType());
                txRequest.setAmount(testTx.getAmount());
                txRequest.setChannel(testTx.getChannel());
                txRequest.setSourceAccount(testTx.getSourceAccount());
                txRequest.setDestinationAccount(testTx.getDestinationAccount());
                txRequest.setTransactionDate(LocalDateTime.now());
                
                // Calculate charges
                ChargeCalculationResult calcResult = calculateChargesForTransaction(txRequest);
                
                // Create test result for this transaction
                ChargeTestResult.TransactionTestResult txResult = new ChargeTestResult.TransactionTestResult();
                txResult.setTransactionType(testTx.getTransactionType());
                txResult.setTransactionAmount(testTx.getAmount());
                txResult.setChannel(testTx.getChannel());
                txResult.setDescription(testTx.getDescription());
                txResult.setCalculationSuccessful(calcResult.isSuccess());
                
                if (calcResult.isSuccess()) {
                    for (ChargeCalculationDetail charge : calcResult.getCalculatedCharges()) {
                        txResult.addCharge(charge);
                    }
                    txResult.setCalculationSummary(calcResult.getCalculationSummary());
                } else {
                    txResult.setErrorMessage(calcResult.getMessage());
                    txResult.setCalculationSummary("Error: " + calcResult.getMessage());
                }
                
                testResult.addTransactionResult(txResult);
                
                // Optionally save test results as actual transactions
                if (request.isSaveResults() && calcResult.isSuccess()) {
                    saveChargeCalculationResults(calcResult);
                }
            }
            
            testResult.setTestSuccessful(true);
            testResult.generateTestSummary();
            
        } catch (Exception e) {
            testResult.setTestSuccessful(false);
            testResult.setTestSummary("Test failed: " + e.getMessage());
        }
        
        return testResult;
    }
    
    /**
     * Process bulk charge calculations
     */
    public BulkChargeCalculationResult processBulkCalculations(BulkChargeCalculationRequest request) {
        long startTime = System.currentTimeMillis();
        
        BulkChargeCalculationResult bulkResult = new BulkChargeCalculationResult();
        bulkResult.setTotalTransactions(request.getTransactions().size());
        bulkResult.setProcessingTimestamp(LocalDateTime.now());
        bulkResult.setBatchId(request.getBatchId());
        bulkResult.setDescription(request.getDescription());
        
        try {
            // Process each transaction
            for (TransactionRequest txRequest : request.getTransactions()) {
                try {
                    ChargeCalculationResult result = calculateChargesForTransaction(txRequest);
                    
                    if (result.isSuccess()) {
                        bulkResult.addSuccessfulResult(result);
                        
                        // Save to database if requested
                        if (request.isSaveResults()) {
                            saveChargeCalculationResults(result);
                        }
                    } else {
                        bulkResult.addFailedResult(txRequest.getTransactionId(), result.getMessage());
                        
                        if (request.isStopOnError()) {
                            break; // Stop processing on first error
                        }
                    }
                    
                } catch (Exception e) {
                    bulkResult.addFailedResult(txRequest.getTransactionId(), e.getMessage());
                    
                    if (request.isStopOnError()) {
                        break;
                    }
                }
            }
            
        } catch (Exception e) {
            bulkResult.setOverallSuccess(false);
            bulkResult.setProcessingMessage("Bulk processing failed: " + e.getMessage());
        }
        
        long endTime = System.currentTimeMillis();
        bulkResult.setProcessingTimeMs(endTime - startTime);
        bulkResult.generateProcessingMessage();
        
        return bulkResult;
    }
    
    /**
     * Find rules applicable to this transaction
     */
    private List<ChargeRule> findApplicableRules(TransactionRequest request, ChargeRule.Category customerCategory) {
        // Get active rules for customer category
        List<ChargeRule> potentialRules = chargeRuleRepository.findActiveRulesForCategory(customerCategory);
        
        // Add rules that apply to ALL categories
        List<ChargeRule> allCategoryRules = chargeRuleRepository.findByCategoryAndStatus(ChargeRule.Category.ALL, ChargeRule.Status.ACTIVE);
        potentialRules.addAll(allCategoryRules);
        
        // Filter rules based on transaction specifics
        return potentialRules.stream()
            .filter(rule -> isRuleApplicableToTransaction(rule, request))
            .collect(Collectors.toList());
    }
    
    /**
     * Check if a rule applies to this specific transaction
     */
    private boolean isRuleApplicableToTransaction(ChargeRule rule, TransactionRequest request) {
        Map<String, Object> conditions = rule.getConditions();
        
        // Check transaction type matching
        if (conditions.containsKey("transaction_type")) {
            String requiredType = conditions.get("transaction_type").toString();
            if (!requiredType.equals(request.getTransactionType())) {
                return false;
            }
        }
        
        // Check amount thresholds
        if (rule.getMinAmount() != null && request.getAmount().compareTo(rule.getMinAmount()) < 0) {
            return false;
        }
        
        if (rule.getMaxAmount() != null && request.getAmount().compareTo(rule.getMaxAmount()) > 0) {
            return false;
        }
        
        // Check channel matching
        if (conditions.containsKey("channel") && request.getChannel() != null) {
            String requiredChannel = conditions.get("channel").toString();
            if (!requiredChannel.equals(request.getChannel())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Calculate charge for a specific rule
     */
    private ChargeCalculationDetail calculateChargeForRule(TransactionRequest request, Customer customer, ChargeRule rule) {
        ChargeCalculationDetail detail = new ChargeCalculationDetail();
        detail.setRuleId(rule.getId());
        detail.setRuleCode(rule.getRuleCode());
        detail.setRuleName(rule.getRuleName());
        detail.setRuleCategory(rule.getCategory().toString());
        detail.setActivityType(rule.getActivityType().toString());
        detail.setFeeType(rule.getFeeType().toString());
        detail.setChargeAmount(BigDecimal.ZERO);
        
        try {
            BigDecimal chargeAmount = BigDecimal.ZERO;
            
            switch (rule.getActivityType()) {
                case UNIT_WISE:
                    chargeAmount = calculateUnitWiseCharge(request, customer, rule);
                    break;
                case RANGE_BASED:
                    chargeAmount = calculateRangeBasedCharge(request, customer, rule);
                    break;
                case MONTHLY:
                    chargeAmount = calculateMonthlyCharge(request, customer, rule);
                    break;
                case SPECIAL:
                    chargeAmount = calculateSpecialCharge(request, customer, rule);
                    break;
                case ADHOC:
                    chargeAmount = calculateAdhocCharge(request, customer, rule);
                    break;
                default:
                    return null;
            }
            
            detail.setChargeAmount(chargeAmount.setScale(2, RoundingMode.HALF_UP));
            detail.setCalculationBasis(buildCalculationBasis(rule, request, chargeAmount));
            
            if (rule.getFeeType() == ChargeRule.FeeType.PERCENTAGE) {
                detail.setAppliedRate(rule.getFeeValue());
            }
            
        } catch (Exception e) {
            detail.setChargeAmount(BigDecimal.ZERO);
            detail.setCalculationBasis("Error calculating charge: " + e.getMessage());
        }
        
        return detail;
    }
    
    /**
     * Calculate unit-wise charges (e.g., per transaction)
     */
    private BigDecimal calculateUnitWiseCharge(TransactionRequest request, Customer customer, ChargeRule rule) {
        if (rule.getFeeType() == ChargeRule.FeeType.PERCENTAGE) {
            return request.getAmount().multiply(rule.getFeeValue()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            return rule.getFeeValue();
        }
    }
    
    /**
     * Calculate range-based charges (e.g., based on monthly transaction count)
     */
    private BigDecimal calculateRangeBasedCharge(TransactionRequest request, Customer customer, ChargeRule rule) {
        // Get transaction count for the current period
        LocalDateTime periodStart = getPeriodStart(rule.getThresholdPeriod()).atStartOfDay();
        LocalDateTime periodEnd = LocalDateTime.now();
        
        int transactionCount = transactionRepository.countTransactionsByCustomerAndTypeAndPeriod(
            customer.getId(), 
            request.getTransactionType(), 
            periodStart, 
            periodEnd
        );
        
        Map<String, Object> conditions = rule.getConditions();
        
        // Check if we exceed the threshold
        if (conditions.containsKey("threshold")) {
            int threshold = Integer.parseInt(conditions.get("threshold").toString());
            if (transactionCount >= threshold) {
                return calculateUnitWiseCharge(request, customer, rule);
            }
        }
        
        // Check for range-based conditions (e.g., 11-30 transactions)
        if (conditions.containsKey("min_count") && conditions.containsKey("max_count")) {
            int minCount = Integer.parseInt(conditions.get("min_count").toString());
            int maxCount = Integer.parseInt(conditions.get("max_count").toString());
            
            if (transactionCount >= minCount && transactionCount <= maxCount) {
                return calculateUnitWiseCharge(request, customer, rule);
            }
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Calculate monthly charges (e.g., fixed monthly fees)
     */
    private BigDecimal calculateMonthlyCharge(TransactionRequest request, Customer customer, ChargeRule rule) {
        // Check if monthly charge already applied this month
        boolean alreadyCharged = chargeCalculationRepository.existsMonthlyChargeForCustomerAndRule(
            customer.getId(), 
            rule.getId()
        );
        
        if (alreadyCharged) {
            return BigDecimal.ZERO; // Already charged this month
        }
        
        return rule.getFeeValue();
    }
    
    /**
     * Calculate special activity charges (e.g., card replacement, statement requests)
     */
    private BigDecimal calculateSpecialCharge(TransactionRequest request, Customer customer, ChargeRule rule) {
        // Special charges are typically flat fees
        return rule.getFeeValue();
    }
    
    /**
     * Calculate adhoc charges (e.g., over-the-counter transactions)
     */
    private BigDecimal calculateAdhocCharge(TransactionRequest request, Customer customer, ChargeRule rule) {
        // Adhoc charges can be percentage or flat based on rule configuration
        return calculateUnitWiseCharge(request, customer, rule);
    }
    
    /**
     * Get period start date based on threshold period
     */
    private LocalDate getPeriodStart(ChargeRule.ThresholdPeriod period) {
        LocalDate now = LocalDate.now();
        
        switch (period) {
            case DAILY:
                return now;
            case MONTHLY:
                return now.withDayOfMonth(1);
            case YEARLY:
                return now.withDayOfYear(1);
            default:
                return now.withDayOfMonth(1);
        }
    }
    
    /**
     * Build explanation of how the charge was calculated
     */
    private String buildCalculationBasis(ChargeRule rule, TransactionRequest request, BigDecimal chargeAmount) {
        StringBuilder basis = new StringBuilder();
        
        basis.append("Rule: ").append(rule.getRuleName()).append(". ");
        
        if (rule.getFeeType() == ChargeRule.FeeType.PERCENTAGE) {
            basis.append("Applied ").append(rule.getFeeValue()).append("% on amount ₹")
                  .append(request.getAmount()).append(" = ₹").append(chargeAmount);
        } else {
            basis.append("Applied flat fee of ₹").append(chargeAmount);
        }
        
        // Add context for range-based rules
        if (rule.getActivityType() == ChargeRule.ActivityType.RANGE_BASED) {
            basis.append(" (based on transaction count in period)");
        }
        
        return basis.toString();
    }
    
    /**
     * Validate transaction request
     */
    private void validateTransactionRequest(TransactionRequest request) {
        if (request.getTransactionId() == null || request.getTransactionId().trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID is required");
        }
        
        if (request.getCustomerCode() == null || request.getCustomerCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer code is required");
        }
        
        if (request.getTransactionType() == null || request.getTransactionType().trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction type is required");
        }
        
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
        
        // Check for duplicate transaction ID
        if (transactionRepository.existsByTransactionId(request.getTransactionId())) {
            throw new IllegalArgumentException("Transaction ID already exists: " + request.getTransactionId());
        }
    }
    
    /**
     * Save charge calculation results to database
     */
    @Transactional
    public void saveChargeCalculationResults(ChargeCalculationResult result) {
        if (!result.isSuccess() || result.getCalculatedCharges().isEmpty()) {
            return;
        }
        
        try {
            // Find customer
            Customer customer = customerRepository.findByCustomerCode(result.getCustomerCode())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            
            // Create transaction record
            Transaction transaction = new Transaction();
            transaction.setTransactionId(result.getTransactionId());
            transaction.setCustomerId(customer.getId());
            transaction.setTransactionType(result.getTransactionType());
            transaction.setAmount(result.getTransactionAmount());
            transaction.setTransactionDate(result.getCalculationTimestamp());
            
            // Set channel if available from metadata
            try {
                transaction.setChannel(Transaction.Channel.API); // Default for calculated transactions
            } catch (Exception e) {
                transaction.setChannel(Transaction.Channel.API);
            }
            
            transaction.setStatus(Transaction.Status.PROCESSED);
            transaction.setProcessedAt(LocalDateTime.now());
            
            transaction = transactionRepository.save(transaction);
            
            // Save charge calculations
            for (ChargeCalculationDetail detail : result.getCalculatedCharges()) {
                ChargeCalculation calculation = new ChargeCalculation();
                calculation.setTransactionId(transaction.getId());
                calculation.setRuleId(detail.getRuleId());
                calculation.setCalculatedAmount(detail.getChargeAmount());
                calculation.setCalculationBasis(detail.getCalculationBasis());
                calculation.setStatus(ChargeCalculation.Status.CALCULATED);
                calculation.setPeriodStart(getPeriodStart(ChargeRule.ThresholdPeriod.MONTHLY));
                calculation.setPeriodEnd(LocalDate.now());
                
                chargeCalculationRepository.save(calculation);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to save charge calculation results: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get customer display name
     */
    private String getCustomerDisplayName(Customer customer) {
        if (customer.getCustomerType() == Customer.CustomerType.RETAIL) {
            return customer.getFirstName() + " " + customer.getLastName();
        } else {
            return customer.getCompanyName();
        }
    }
    
    /**
     * Get predefined test scenarios
     */
    public Map<String, Object> getTestScenarios() {
        Map<String, Object> scenarios = new HashMap<>();
        
        // ATM Withdrawal Test Scenarios
        List<Map<String, Object>> atmScenarios = new ArrayList<>();
        atmScenarios.add(createTestScenario("ATM_WITHDRAWAL_PARENT", new BigDecimal("1000"), "ATM", "Normal ATM withdrawal from parent bank"));
        atmScenarios.add(createTestScenario("ATM_WITHDRAWAL_PARENT", new BigDecimal("5000"), "ATM", "High value ATM withdrawal"));
        atmScenarios.add(createTestScenario("ATM_WITHDRAWAL_OTHER", new BigDecimal("2000"), "ATM", "ATM withdrawal from other bank"));
        
        // Funds Transfer Test Scenarios
        List<Map<String, Object>> transferScenarios = new ArrayList<>();
        transferScenarios.add(createTestScenario("FUNDS_TRANSFER", new BigDecimal("500"), "ONLINE", "Small online funds transfer"));
        transferScenarios.add(createTestScenario("FUNDS_TRANSFER", new BigDecimal("10000"), "ONLINE", "Medium online funds transfer"));
        transferScenarios.add(createTestScenario("FUNDS_TRANSFER", new BigDecimal("50000"), "BRANCH", "Large branch funds transfer"));
        
        // Special Services Test Scenarios
        List<Map<String, Object>> specialScenarios = new ArrayList<>();
        specialScenarios.add(createTestScenario("STATEMENT_PRINT", new BigDecimal("1"), "BRANCH", "Statement print request"));
        specialScenarios.add(createTestScenario("DUPLICATE_DEBIT_CARD", new BigDecimal("1"), "BRANCH", "Duplicate debit card request"));
        specialScenarios.add(createTestScenario("DUPLICATE_CREDIT_CARD", new BigDecimal("1"), "BRANCH", "Duplicate credit card request"));
        
        scenarios.put("atm_scenarios", atmScenarios);
        scenarios.put("transfer_scenarios", transferScenarios);
        scenarios.put("special_scenarios", specialScenarios);
        
        // Sample customers for testing
        List<Customer> sampleCustomers = customerRepository.findByStatus(Customer.Status.ACTIVE);
        List<Map<String, String>> customerList = new ArrayList<>();
        
        for (Customer customer : sampleCustomers) {
            Map<String, String> customerInfo = new HashMap<>();
            customerInfo.put("code", customer.getCustomerCode());
            customerInfo.put("name", getCustomerDisplayName(customer));
            customerInfo.put("type", customer.getCustomerType().toString());
            customerList.add(customerInfo);
        }
        
        scenarios.put("sample_customers", customerList);
        
        return scenarios;
    }
    
    /**
     * Helper method to create test scenario
     */
    private Map<String, Object> createTestScenario(String transactionType, BigDecimal amount, String channel, String description) {
        Map<String, Object> scenario = new HashMap<>();
        scenario.put("transactionType", transactionType);
        scenario.put("amount", amount);
        scenario.put("channel", channel);
        scenario.put("description", description);
        return scenario;
    }
}