-- ===============================================================================
-- Insert Missing Charge Rules (Rules 004, 005, 006, 007, 010, 011)
-- Run this script to add the missing rules to your database
-- ===============================================================================

USE charge_management_dev;

-- First, check if rules already exist to avoid duplicates
-- If you get duplicate key errors, these rules already exist

-- ============================================================================
-- Rule 004: Statement Print Copy - On Demand - ₹50
-- ============================================================================
INSERT INTO charge_rules (
    rule_code, rule_name, category, activity_type, 
    conditions_json, fee_type, fee_value, 
    status, created_by, effective_from
) VALUES (
    '004', 
    'Statement Print Copy', 
    'RETAIL_BANKING', 
    'SPECIAL',
    '{"transaction_type": "STATEMENT_PRINT", "frequency": "ON_DEMAND"}',
    'FLAT_AMOUNT',
    50.0000,
    'ACTIVE',
    1,
    CURRENT_TIMESTAMP
);

-- ============================================================================
-- Rule 005: Duplicate Debit Card - On Demand - ₹150
-- ============================================================================
INSERT INTO charge_rules (
    rule_code, rule_name, category, activity_type, 
    conditions_json, fee_type, fee_value, 
    status, created_by, effective_from
) VALUES (
    '005', 
    'Duplicate Debit Card', 
    'RETAIL_BANKING', 
    'SPECIAL',
    '{"transaction_type": "DUPLICATE_DEBIT_CARD", "frequency": "ON_DEMAND"}',
    'FLAT_AMOUNT',
    150.0000,
    'ACTIVE',
    1,
    CURRENT_TIMESTAMP
);

-- ============================================================================
-- Rule 006: Duplicate Credit Card - On Demand - ₹450
-- ============================================================================
INSERT INTO charge_rules (
    rule_code, rule_name, category, activity_type, 
    conditions_json, fee_type, fee_value, 
    status, created_by, effective_from
) VALUES (
    '006', 
    'Duplicate Credit Card', 
    'RETAIL_BANKING', 
    'SPECIAL',
    '{"transaction_type": "DUPLICATE_CREDIT_CARD", "frequency": "ON_DEMAND"}',
    'FLAT_AMOUNT',
    450.0000,
    'ACTIVE',
    1,
    CURRENT_TIMESTAMP
);

-- ============================================================================
-- Rule 007: ATM Withdrawals from Other Bank > 5/month - 10% charge
-- ============================================================================
INSERT INTO charge_rules (
    rule_code, rule_name, category, activity_type, 
    conditions_json, fee_type, fee_value, 
    threshold_count, threshold_period,
    status, created_by, effective_from
) VALUES (
    '007', 
    'Number of ATM Withdrawals from another bank machine', 
    'RETAIL_BANKING', 
    'RANGE_BASED',
    '{"transaction_type": "ATM_WITHDRAWAL_OTHER", "threshold": 5, "period": "MONTHLY"}',
    'PERCENTAGE',
    10.0000,
    5,
    'MONTHLY',
    'ACTIVE',
    1,
    CURRENT_TIMESTAMP
);

-- ============================================================================
-- Rule 010: Funds Transfer 31-50/month - ₹150
-- ============================================================================
INSERT INTO charge_rules (
    rule_code, rule_name, category, activity_type, 
    conditions_json, fee_type, fee_value, 
    status, created_by, effective_from
) VALUES (
    '010', 
    'Funds Transfer - 31 to 50 in a Month', 
    'ALL', 
    'RANGE_BASED',
    '{"transaction_type": "FUNDS_TRANSFER", "min_count": 31, "max_count": 50, "period": "MONTHLY"}',
    'FLAT_AMOUNT',
    150.0000,
    'ACTIVE',
    1,
    CURRENT_TIMESTAMP
);

-- ============================================================================
-- Rule 011: Funds Transfer > 51/month - ₹300 flat
-- ============================================================================
INSERT INTO charge_rules (
    rule_code, rule_name, category, activity_type, 
    conditions_json, fee_type, fee_value, 
    status, created_by, effective_from
) VALUES (
    '011', 
    'Funds Transfer - More than 51', 
    'ALL', 
    'RANGE_BASED',
    '{"transaction_type": "FUNDS_TRANSFER", "min_count": 51, "period": "MONTHLY"}',
    'FLAT_AMOUNT',
    300.0000,
    'ACTIVE',
    1,
    CURRENT_TIMESTAMP
);

-- ============================================================================
-- Verify all rules were inserted successfully
-- ============================================================================
SELECT 
    rule_code,
    rule_name,
    category,
    activity_type,
    fee_type,
    fee_value,
    status,
    created_at
FROM charge_rules
ORDER BY rule_code;

-- ============================================================================
-- Expected Output: You should now see all 11 rules (001-011)
-- ============================================================================

COMMIT;