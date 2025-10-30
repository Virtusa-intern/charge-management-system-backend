package com.bank.charge_management_system.service;

import com.bank.charge_management_system.dto.*;
import com.bank.charge_management_system.entity.*;
import com.bank.charge_management_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private ThreadLocal<Map<String, Map<String, Integer>>> inMemoryTransactionCounts = ThreadLocal.withInitial(HashMap::new);

    /**
     * Calculate charges for a single transaction
     */
    public ChargeCalculationResult calculateChargesForTransaction(TransactionRequest request) {
        ChargeCalculationResult result = new ChargeCalculationResult();
        result.setTransactionId(request.getTransactionId());
        result.setCustomerCode(request.getCustomerCode());
        result.setTransactionType(request.getTransactionType());
        result.setTransactionAmount(request.getAmount());
        result.setChannel(request.getChannel()); // Set the channel from request
        result.setCalculationTimestamp(LocalDateTime.now());
        
        try {
            if (inMemoryTransactionCounts.get() == null) {
                inMemoryTransactionCounts.set(new HashMap<>());
            }

            validateTransactionRequest(request);
            
            Customer customer = customerRepository.findByCustomerCode(request.getCustomerCode())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.getCustomerCode()));
            
            ChargeRule.Category customerCategory = customer.getCustomerType() == Customer.CustomerType.RETAIL 
                ? ChargeRule.Category.RETAIL_BANKING 
                : ChargeRule.Category.CORP_BANKING;
            
            List<ChargeRule> applicableRules = findApplicableRules(request, customerCategory);
            
            for (ChargeRule rule : applicableRules) {
                BigDecimal chargeAmount = calculateChargeForSpecificRule(request, customer, rule);
                
                if (chargeAmount.compareTo(BigDecimal.ZERO) > 0) {
                    ChargeCalculationDetail detail = new ChargeCalculationDetail();
                    detail.setRuleId(rule.getId());
                    detail.setRuleCode(rule.getRuleCode());
                    detail.setRuleName(rule.getRuleName());
                    detail.setRuleCategory(rule.getCategory().toString());
                    detail.setActivityType(rule.getActivityType().toString());
                    detail.setFeeType(rule.getFeeType().toString());
                    detail.setChargeAmount(chargeAmount.setScale(2, RoundingMode.HALF_UP));
                    detail.setCalculationBasis(buildCalculationBasis(rule, request, chargeAmount));
                    
                    if (rule.getFeeType() == ChargeRule.FeeType.PERCENTAGE) {
                        detail.setAppliedRate(rule.getFeeValue());
                    }
                    
                    result.addCharge(detail);
                }
            }
            
            result.setSuccess(true);
            result.setMessage("Charges calculated successfully");
            result.generateSummary();

            try {
            saveChargeCalculationResults(result);
            System.out.println("✅ Transaction saved to database: " + request.getTransactionId());
            } catch (Exception saveEx) {
                System.err.println("⚠️ Warning: Could not save transaction: " + saveEx.getMessage());
                // Don't fail the calculation if save fails
            }

            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Charge calculation failed: " + e.getMessage());
            result.setCalculationSummary("Error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Calculate charge for specific rule based on rule code
     */
    private BigDecimal calculateChargeForSpecificRule(TransactionRequest request, Customer customer, ChargeRule rule) {
        try {
            switch (rule.getRuleCode()) {
                case "001":
                    return calculateRule001_ATMWithdrawals(request, customer);
                case "002":
                    return calculateRule002_MonthlySavings(request, customer, rule);
                case "003":
                    return calculateRule003_CorporateAccount(request, customer, rule);
                case "007":
                    return calculateRule007_ATMOtherBank(request, customer);
                case "008":
                    return calculateRule008_FundsTransferFree(request, customer);
                case "009":
                    return calculateRule009_FundsTransferStandard(request, customer);
                case "010":
                    return calculateRule010_FundsTransferHigh(request, customer);
                case "011":
                    return calculateRule011_FundsTransferPremium(request, customer);
                case "004":
                    return calculateRule004_StatementPrint(request);
                case "005":
                    return calculateRule005_DuplicateDebitCard(request);
                case "006":
                    return calculateRule006_DuplicateCreditCard(request);
                default:
                    return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            System.err.println("Error in rule " + rule.getRuleCode() + ": " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    // ========== BUSINESS RULE IMPLEMENTATIONS ==========

    /**
     * Rule 001: ATM Withdrawals from parent bank > 20/month = 2% charge
     */
    private BigDecimal calculateRule001_ATMWithdrawals(TransactionRequest request, Customer customer) {
        if (!"ATM_WITHDRAWAL_PARENT".equals(request.getTransactionType())) {
            return BigDecimal.ZERO;
        }
        
        int dbCount = transactionRepository.getMonthlyTransactionCountByType(customer.getId(), "ATM_WITHDRAWAL_PARENT");
        int memoryCount = getInMemoryTransactionCount(customer.getId(), "ATM_WITHDRAWAL_PARENT");
        int totalCount = dbCount + memoryCount + 1;
        
        // Charge applies AFTER 20 withdrawals (i.e., on 21st and onwards)
        if (totalCount > 20) {
            incrementInMemoryCount(customer.getId(), "ATM_WITHDRAWAL_PARENT");
            return request.getAmount()
                .multiply(BigDecimal.valueOf(2))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        
        incrementInMemoryCount(customer.getId(), "ATM_WITHDRAWAL_PARENT");
        return BigDecimal.ZERO;
    }

    /**
     * Rule 002: Monthly Charges - Savings Account = ₹25/month
     */
    private BigDecimal calculateRule002_MonthlySavings(TransactionRequest request, Customer customer, ChargeRule rule) {
        // ✅ FIX: Only apply for specific transaction type
        if (!"MONTHLY_SAVINGS_CHARGE".equals(request.getTransactionType())) {
            return BigDecimal.ZERO;
        }
        
        if (customer.getCustomerType() != Customer.CustomerType.RETAIL) {
            return BigDecimal.ZERO;
        }
        
        // Check if already charged this month
        boolean alreadyCharged = chargeCalculationRepository.existsMonthlyChargeForCustomerAndRule(
            customer.getId(), rule.getId());
        
        if (alreadyCharged) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.valueOf(25);
    }

    /**
     * Rule 003: Bi-Monthly Charges - Current Account (Corporate) = 5% of bi-monthly average
     */
    private BigDecimal calculateRule003_CorporateAccount(TransactionRequest request, Customer customer, ChargeRule rule) {
        // ✅ FIX: Only apply for specific transaction type
        if (!"CORPORATE_BI_MONTHLY_CHARGE".equals(request.getTransactionType())) {
            return BigDecimal.ZERO;
        }
        
        if (customer.getCustomerType() != Customer.CustomerType.CORPORATE) {
            return BigDecimal.ZERO;
        }

        LocalDate sixtyDaysAgo = LocalDate.now().minusDays(60);
        boolean alreadyCharged = chargeCalculationRepository.existsBiMonthlyChargeForCustomerAndRule(
            customer.getId(), rule.getId(), sixtyDaysAgo);
        
        if (alreadyCharged) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal averageBalance = transactionRepository.getAverageBalanceForLastTwoMonths(customer.getId());
        return averageBalance
            .multiply(BigDecimal.valueOf(5))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Rule 007: ATM Withdrawals from other bank > 5/month = 10% charge
     */
    private BigDecimal calculateRule007_ATMOtherBank(TransactionRequest request, Customer customer) {
        if (!"ATM_WITHDRAWAL_OTHER".equals(request.getTransactionType())) {
            return BigDecimal.ZERO;
        }
        
        int dbCount = transactionRepository.getMonthlyTransactionCountByType(customer.getId(), "ATM_WITHDRAWAL_OTHER");
        int memoryCount = getInMemoryTransactionCount(customer.getId(), "ATM_WITHDRAWAL_OTHER");
        int totalCount = dbCount + memoryCount + 1; // ✅ +1
        
        // Charge applies AFTER 5 withdrawals (i.e., on 6th and onwards)
        if (totalCount > 5) {
            incrementInMemoryCount(customer.getId(), "ATM_WITHDRAWAL_OTHER");
            return request.getAmount()
                .multiply(BigDecimal.valueOf(10))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        
        incrementInMemoryCount(customer.getId(), "ATM_WITHDRAWAL_OTHER");
        return BigDecimal.ZERO;
    }

    /**
     * Rule 008: Funds Transfer < 10 in a month = FREE
     * FIXED: Returns ZERO for transactions 1-10
     */
    private BigDecimal calculateRule008_FundsTransferFree(TransactionRequest request, Customer customer) {
        int dbCount = transactionRepository.getMonthlyTransactionCountByType(customer.getId(), "FUNDS_TRANSFER");
        int memoryCount = getInMemoryTransactionCount(customer.getId(), "FUNDS_TRANSFER");
        int totalCount = dbCount + memoryCount + 1; // +1 for CURRENT transaction
        
        // First 10 are FREE (transactions 1-10)
        return totalCount <= 10 ? BigDecimal.ZERO : BigDecimal.ZERO; // Still returns 0, but logic is clear
    }

    /**
     * Rule 009: Funds Transfer 11-30 in a month = ₹100
     * FIXED: Now correctly charges on transactions 11-30 (inclusive)
     */
    private BigDecimal calculateRule009_FundsTransferStandard(TransactionRequest request, Customer customer) {
        int dbCount = transactionRepository.getMonthlyTransactionCountByType(customer.getId(), "FUNDS_TRANSFER");
        int memoryCount = getInMemoryTransactionCount(customer.getId(), "FUNDS_TRANSFER");
        int totalCount = dbCount + memoryCount + 1; // +1 for CURRENT transaction
        
        // Transactions 11-30 charge ₹100
        return (totalCount >= 11 && totalCount <= 30) ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
    }

    /**
     * Rule 010: Funds Transfer 31-50 in a month = ₹150
     * FIXED: Now correctly charges on transactions 31-50 (inclusive)
     */
    private BigDecimal calculateRule010_FundsTransferHigh(TransactionRequest request, Customer customer) {
        int dbCount = transactionRepository.getMonthlyTransactionCountByType(customer.getId(), "FUNDS_TRANSFER");
        int memoryCount = getInMemoryTransactionCount(customer.getId(), "FUNDS_TRANSFER");
        int totalCount = dbCount + memoryCount + 1; // +1 for CURRENT transaction
        
        // Transactions 31-50 charge ₹150
        return (totalCount >= 31 && totalCount <= 50) ? BigDecimal.valueOf(150) : BigDecimal.ZERO;
    }

    /**
     * Rule 011: Funds Transfer > 51 in a month = ₹300 flat
     * FIXED: Now correctly charges on transactions 51+ (inclusive)
     */
    private BigDecimal calculateRule011_FundsTransferPremium(TransactionRequest request, Customer customer) {
        int dbCount = transactionRepository.getMonthlyTransactionCountByType(customer.getId(), "FUNDS_TRANSFER");
        int memoryCount = getInMemoryTransactionCount(customer.getId(), "FUNDS_TRANSFER");
        int totalCount = dbCount + memoryCount + 1; // +1 for CURRENT transaction
        
        // Transactions 51+ charge ₹300
        return totalCount >= 51 ? BigDecimal.valueOf(300) : BigDecimal.ZERO;
    }

    /**
     * Rule 004: Statement Print = ₹50
     */
    private BigDecimal calculateRule004_StatementPrint(TransactionRequest request) {
        return "STATEMENT_PRINT".equals(request.getTransactionType()) 
            ? BigDecimal.valueOf(50) 
            : BigDecimal.ZERO;
    }

    /**
     * Rule 005: Duplicate Debit Card = ₹150
     */
    private BigDecimal calculateRule005_DuplicateDebitCard(TransactionRequest request) {
        return "DUPLICATE_DEBIT_CARD".equals(request.getTransactionType()) 
            ? BigDecimal.valueOf(150) 
            : BigDecimal.ZERO;
    }

    /**
     * Rule 006: Duplicate Credit Card = ₹450
     */
    private BigDecimal calculateRule006_DuplicateCreditCard(TransactionRequest request) {
        return "DUPLICATE_CREDIT_CARD".equals(request.getTransactionType()) 
            ? BigDecimal.valueOf(450) 
            : BigDecimal.ZERO;
    }

    // ========== HELPER METHODS ==========
    
    public ChargeTestResult testChargeCalculation(ChargeTestRequest request) {
        ChargeTestResult testResult = new ChargeTestResult();
        testResult.setCustomerCode(request.getCustomerCode());
        testResult.setTestTimestamp(LocalDateTime.now());
        testResult.setTestDescription(request.getTestDescription());

        try {
            inMemoryTransactionCounts.set(new HashMap<>()); // Clear/init at start
            Customer customer = customerRepository.findByCustomerCode(request.getCustomerCode())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.getCustomerCode()));
            
            testResult.setCustomerName(getCustomerDisplayName(customer));
            testResult.setCustomerType(customer.getCustomerType().toString());
            
            int transactionIndex = 0;
            
            for (ChargeTestRequest.TestTransaction testTx : request.getTestTransactions()) {
                transactionIndex++;
                String testTransactionId = "TEST_" + request.getCustomerCode() + "_" + System.currentTimeMillis() + "_" + transactionIndex;
                
                TransactionRequest txRequest = new TransactionRequest();
                txRequest.setTransactionId(testTransactionId);
                txRequest.setCustomerCode(request.getCustomerCode());
                txRequest.setTransactionType(testTx.getTransactionType());
                txRequest.setAmount(testTx.getAmount());
                txRequest.setChannel(testTx.getChannel());
                txRequest.setSourceAccount(testTx.getSourceAccount());
                txRequest.setDestinationAccount(testTx.getDestinationAccount());
                txRequest.setTransactionDate(LocalDateTime.now());
                
                ChargeCalculationResult calcResult = calculateChargesForTransaction(txRequest);
                
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
                
                if (request.isSaveResults() && calcResult.isSuccess()) {
                    saveChargeCalculationResults(calcResult);
                }
            }
            
            testResult.setTestSuccessful(true);
            testResult.generateTestSummary();
            
        } catch (Exception e) {
            testResult.setTestSuccessful(false);
            testResult.setTestSummary("Test failed: " + e.getMessage());
        } finally {
            inMemoryTransactionCounts.remove(); // Clear at end
        }
        
        return testResult;
    }
    
    public BulkChargeCalculationResult processBulkCalculations(BulkChargeCalculationRequest request) {
        long startTime = System.currentTimeMillis();
        
        BulkChargeCalculationResult bulkResult = new BulkChargeCalculationResult();
        bulkResult.setTotalTransactions(request.getTransactions().size());
        bulkResult.setProcessingTimestamp(LocalDateTime.now());
        bulkResult.setBatchId(request.getBatchId());
        bulkResult.setDescription(request.getDescription());
        
        try {
            inMemoryTransactionCounts.set(new HashMap<>()); // Clear/init at start
            for (TransactionRequest txRequest : request.getTransactions()) {
                try {
                    ChargeCalculationResult result = calculateChargesForTransaction(txRequest);
                    
                    if (result.isSuccess()) {
                        bulkResult.addSuccessfulResult(result);
                        
                        if (request.isSaveResults()) {
                            saveChargeCalculationResults(result);
                        }
                    } else {
                        bulkResult.addFailedResult(txRequest.getTransactionId(), result.getMessage());
                        
                        if (request.isStopOnError()) break;
                    }
                    
                } catch (Exception e) {
                    bulkResult.addFailedResult(txRequest.getTransactionId(), e.getMessage());
                    if (request.isStopOnError()) break;
                }
            }
        } catch (Exception e) {
            bulkResult.setOverallSuccess(false);
            bulkResult.setProcessingMessage("Bulk processing failed: " + e.getMessage());
        } finally {
            inMemoryTransactionCounts.remove(); // Clear at the end
        }
        
        long endTime = System.currentTimeMillis();
        bulkResult.setProcessingTimeMs(endTime - startTime);
        bulkResult.generateProcessingMessage();
        
        return bulkResult;
    }
    
    private List<ChargeRule> findApplicableRules(TransactionRequest request, ChargeRule.Category customerCategory) {
        List<ChargeRule> potentialRules = chargeRuleRepository.findActiveRulesForCategory(customerCategory);
        List<ChargeRule> allCategoryRules = chargeRuleRepository.findByCategoryAndStatus(ChargeRule.Category.ALL, ChargeRule.Status.ACTIVE);
        potentialRules.addAll(allCategoryRules);
        
        return potentialRules.stream()
            .filter(rule -> isRuleApplicableToTransaction(rule, request))
            .collect(Collectors.toList());
    }
    
    private boolean isRuleApplicableToTransaction(ChargeRule rule, TransactionRequest request) {
        Map<String, Object> conditions = rule.getConditions();
        
        if (conditions.containsKey("transaction_type")) {
            String requiredType = conditions.get("transaction_type").toString();
            if (!requiredType.equals(request.getTransactionType())) {
                return false;
            }
        }
        
        if (rule.getMinAmount() != null && request.getAmount().compareTo(rule.getMinAmount()) < 0) {
            return false;
        }
        
        if (rule.getMaxAmount() != null && request.getAmount().compareTo(rule.getMaxAmount()) > 0) {
            return false;
        }
        
        return true;
    }
    
    private String buildCalculationBasis(ChargeRule rule, TransactionRequest request, BigDecimal chargeAmount) {
        StringBuilder basis = new StringBuilder();
        basis.append("Rule: ").append(rule.getRuleName()).append(". ");
        
        if (rule.getFeeType() == ChargeRule.FeeType.PERCENTAGE) {
            basis.append("Applied ").append(rule.getFeeValue()).append("% on amount ₹")
                  .append(request.getAmount()).append(" = ₹").append(chargeAmount);
        } else {
            basis.append("Applied flat fee of ₹").append(chargeAmount);
        }
        
        if (rule.getActivityType() == ChargeRule.ActivityType.RANGE_BASED) {
            basis.append(" (based on transaction count in period)");
        }
        
        return basis.toString();
    }
    
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
        
        if (transactionRepository.existsByTransactionId(request.getTransactionId())) {
            throw new IllegalArgumentException("Transaction ID already exists: " + request.getTransactionId());
        }
    }
    
    @Transactional
    public void saveChargeCalculationResults(ChargeCalculationResult result) {
        // Save transaction even if there are no charges (for history tracking)
        if (!result.isSuccess()) {
            return;
        }
        
        try {
            Customer customer = customerRepository.findByCustomerCode(result.getCustomerCode())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            
            Transaction transaction = new Transaction();
            transaction.setTransactionId(result.getTransactionId());
            transaction.setCustomerId(customer.getId());
            transaction.setTransactionType(result.getTransactionType());
            transaction.setAmount(result.getTransactionAmount());
            transaction.setTransactionDate(result.getCalculationTimestamp());
            
            // Set channel from result, default to API if not provided
            if (result.getChannel() != null && !result.getChannel().isEmpty()) {
                try {
                    transaction.setChannel(Transaction.Channel.valueOf(result.getChannel().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    transaction.setChannel(Transaction.Channel.API); // Fallback to API
                }
            } else {
                transaction.setChannel(Transaction.Channel.API);
            }
            
            transaction.setStatus(Transaction.Status.PROCESSED);
            transaction.setProcessedAt(LocalDateTime.now());
            
            transaction = transactionRepository.save(transaction);
            
            // Save charge calculations only if there are any charges
            for (ChargeCalculationDetail detail : result.getCalculatedCharges()) {
                ChargeCalculation calculation = new ChargeCalculation();
                calculation.setTransactionId(transaction.getId());
                calculation.setRuleId(detail.getRuleId());
                calculation.setCalculatedAmount(detail.getChargeAmount());
                calculation.setCalculationBasis(detail.getCalculationBasis());
                calculation.setStatus(ChargeCalculation.Status.CALCULATED);
                calculation.setPeriodStart(LocalDate.now().withDayOfMonth(1));
                calculation.setPeriodEnd(LocalDate.now());
                
                chargeCalculationRepository.save(calculation);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to save charge calculation results: " + e.getMessage(), e);
        }
    }
    
    private String getCustomerDisplayName(Customer customer) {
        if (customer.getCustomerType() == Customer.CustomerType.RETAIL) {
            return customer.getFirstName() + " " + customer.getLastName();
        } else {
            return customer.getCompanyName();
        }
    }
    
    public Map<String, Object> getTestScenarios() {
        Map<String, Object> scenarios = new HashMap<>();
        
        List<Map<String, Object>> atmScenarios = new ArrayList<>();
        atmScenarios.add(createTestScenario("ATM_WITHDRAWAL_PARENT", new BigDecimal("1000"), "ATM", "Normal ATM withdrawal from parent bank"));
        atmScenarios.add(createTestScenario("ATM_WITHDRAWAL_PARENT", new BigDecimal("5000"), "ATM", "High value ATM withdrawal"));
        atmScenarios.add(createTestScenario("ATM_WITHDRAWAL_OTHER", new BigDecimal("2000"), "ATM", "ATM withdrawal from other bank"));
        
        List<Map<String, Object>> transferScenarios = new ArrayList<>();
        transferScenarios.add(createTestScenario("FUNDS_TRANSFER", new BigDecimal("500"), "ONLINE", "Small online funds transfer"));
        transferScenarios.add(createTestScenario("FUNDS_TRANSFER", new BigDecimal("10000"), "ONLINE", "Medium online funds transfer"));
        transferScenarios.add(createTestScenario("FUNDS_TRANSFER", new BigDecimal("50000"), "BRANCH", "Large branch funds transfer"));
        
        List<Map<String, Object>> specialScenarios = new ArrayList<>();
        specialScenarios.add(createTestScenario("STATEMENT_PRINT", new BigDecimal("1"), "BRANCH", "Statement print request"));
        specialScenarios.add(createTestScenario("DUPLICATE_DEBIT_CARD", new BigDecimal("1"), "BRANCH", "Duplicate debit card request"));
        specialScenarios.add(createTestScenario("DUPLICATE_CREDIT_CARD", new BigDecimal("1"), "BRANCH", "Duplicate credit card request"));
        
        scenarios.put("atm_scenarios", atmScenarios);
        scenarios.put("transfer_scenarios", transferScenarios);
        scenarios.put("special_scenarios", specialScenarios);
        
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
    
    private Map<String, Object> createTestScenario(String transactionType, BigDecimal amount, String channel, String description) {
        Map<String, Object> scenario = new HashMap<>();
        scenario.put("transactionType", transactionType);
        scenario.put("amount", amount);
        scenario.put("channel", channel);
        scenario.put("description", description);
        return scenario;
    }

    private void incrementInMemoryCount(Long customerId, String transactionType) {
        Map<String, Map<String, Integer>> customerCounts = inMemoryTransactionCounts.get();
        if (customerCounts == null) {
            customerCounts = new HashMap<>();
            inMemoryTransactionCounts.set(customerCounts);
        }
        Map<String, Integer> transactionCounts = customerCounts.computeIfAbsent(String.valueOf(customerId), k -> new HashMap<>());
        transactionCounts.put(transactionType, transactionCounts.getOrDefault(transactionType, 0) + 1);
    }

    private int getInMemoryTransactionCount(Long customerId, String transactionType) {
        Map<String, Map<String, Integer>> customerCounts = inMemoryTransactionCounts.get();
        if (customerCounts == null) {
            return 0;
        }
        return customerCounts.getOrDefault(String.valueOf(customerId), Collections.emptyMap()).getOrDefault(transactionType, 0);
    }

    public List<TransactionHistoryDto> getTransactionHistory(String customerCode) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerCode));

        List<Transaction> transactions = transactionRepository.findByCustomerId(customer.getId());

        return transactions.stream()
                .map(transaction -> new TransactionHistoryDto(
                        transaction.getTransactionId(),
                        transaction.getTransactionType(),
                        transaction.getAmount(),
                        transaction.getTransactionDate(),
                        transaction.getChannel().toString(),
                        transaction.getStatus().toString()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get paginated and filtered transaction history for a customer
     */
    public Page<TransactionHistoryDto> getTransactionHistoryPaged(
            String customerCode,
            String transactionType,
            String channel,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerCode));

        // Convert string params to enums
        Transaction.Channel channelEnum = channel != null ? Transaction.Channel.valueOf(channel.toUpperCase()) : null;
        Transaction.Status statusEnum = status != null ? Transaction.Status.valueOf(status.toUpperCase()) : null;

        // Create pageable with sorting
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Transaction> transactionPage = transactionRepository.findByCustomerIdWithFilters(
                customer.getId(),
                transactionType,
                channelEnum,
                statusEnum,
                startDate,
                endDate,
                pageable
        );

        return transactionPage.map(transaction -> new TransactionHistoryDto(
                transaction.getTransactionId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getChannel().toString(),
                transaction.getStatus().toString()
        ));
    }
}