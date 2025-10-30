-- =====================================================================
-- COMPLETE DATABASE SETUP FOR CHARGE MANAGEMENT SYSTEM
-- =====================================================================
-- This script combines all database initialization scripts into one file
-- Run this file to completely setup your MySQL database
-- 
-- Contents:
-- 1. Database Creation
-- 2. Table Schema Design (8 tables + 2 views)
-- 3. Indexes and Foreign Keys
-- 4. Sample Users (4 users with different roles)
-- 5. All 11 Charge Rules
-- 6. Sample Customers (3 customers)
-- 7. Verification Queries
-- 
-- Prerequisites:
-- - MySQL 8.0 or higher
-- - Sufficient privileges to create database and tables
-- 
-- Usage:
-- Method 1 (MySQL Workbench): File > Open SQL Script > Run
-- Method 2 (Command Line): mysql -u root -p < complete-database-setup.sql
-- 
-- =====================================================================

-- =====================================================================
-- SECTION 1: DATABASE CREATION
-- =====================================================================

CREATE DATABASE IF NOT EXISTS charge_management_dev;
USE charge_management_dev;

-- Display database info
SELECT 'Creating database: charge_management_dev' AS Status;

-- =====================================================================
-- SECTION 2: TABLE SCHEMA DESIGN
-- =====================================================================

-- Temporarily disable foreign key checks for clean setup
SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing tables if they exist (for clean setup)
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS settlement_details;
DROP TABLE IF EXISTS settlement_requests;
DROP TABLE IF EXISTS charge_calculations;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS charge_rules;
DROP TABLE IF EXISTS users;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- TABLE 1: USERS - For authentication and role management
-- =============================================================================

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role ENUM('ADMIN', 'RULE_CREATOR', 'RULE_APPROVER', 'RULE_VIEWER') NOT NULL DEFAULT 'RULE_VIEWER',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'Created table: users' AS Status;

-- =============================================================================
-- TABLE 2: CHARGE_RULES - Core table for storing all charging rules
-- =============================================================================

CREATE TABLE charge_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_code VARCHAR(10) UNIQUE NOT NULL,
    rule_name VARCHAR(100) NOT NULL,
    category ENUM('RETAIL_BANKING', 'CORP_BANKING', 'ALL') NOT NULL,
    activity_type ENUM('UNIT_WISE', 'RANGE_BASED', 'MONTHLY', 'SPECIAL', 'ADHOC') NOT NULL,
    
    -- Condition fields
    conditions_json JSON NOT NULL, -- Store complex conditions as JSON
    
    -- Fee calculation fields  
    fee_type ENUM('PERCENTAGE', 'FLAT_AMOUNT', 'TIERED') NOT NULL,
    fee_value DECIMAL(10,4) NOT NULL, -- Can store percentage or amount
    currency_code VARCHAR(3) DEFAULT 'INR',
    
    -- Business logic fields
    min_amount DECIMAL(15,2) DEFAULT 0,
    max_amount DECIMAL(15,2),
    threshold_count INT DEFAULT 0,
    threshold_period ENUM('DAILY', 'MONTHLY', 'YEARLY') DEFAULT 'MONTHLY',
    
    -- Status and audit fields
    status ENUM('DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED') DEFAULT 'DRAFT',
    effective_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    effective_to TIMESTAMP NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    approved_by BIGINT,
    approved_at TIMESTAMP NULL,
    
    INDEX idx_rule_code (rule_code),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_activity_type (activity_type),
    INDEX idx_effective_dates (effective_from, effective_to),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'Created table: charge_rules' AS Status;

-- =============================================================================
-- TABLE 3: CUSTOMERS - Customer information for transaction processing
-- =============================================================================

CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_code VARCHAR(20) UNIQUE NOT NULL,
    customer_type ENUM('RETAIL', 'CORPORATE') NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    company_name VARCHAR(100), -- For corporate customers
    email VARCHAR(100),
    phone VARCHAR(20),
    
    -- Account status
    status ENUM('ACTIVE', 'INACTIVE', 'BLOCKED') DEFAULT 'ACTIVE',
    registration_date DATE NOT NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_customer_code (customer_code),
    INDEX idx_customer_type (customer_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'Created table: customers' AS Status;

-- =============================================================================
-- TABLE 4: TRANSACTIONS - All banking transactions that may incur charges
-- =============================================================================

CREATE TABLE transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id VARCHAR(50) UNIQUE NOT NULL, -- External transaction ID
    customer_id BIGINT NOT NULL,
    
    -- Transaction details
    transaction_type VARCHAR(50) NOT NULL, -- ATM_WITHDRAWAL, FUNDS_TRANSFER, etc.
    amount DECIMAL(15,2) NOT NULL,
    currency_code VARCHAR(3) DEFAULT 'INR',
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Transaction context (for rule evaluation)
    channel ENUM('ATM', 'ONLINE', 'BRANCH', 'MOBILE', 'API') NOT NULL,
    source_account VARCHAR(50),
    destination_account VARCHAR(50),
    
    -- Metadata for rule processing
    metadata_json JSON, -- Additional data for complex rule evaluation
    
    -- Processing status
    status ENUM('PENDING', 'PROCESSED', 'FAILED') DEFAULT 'PENDING',
    processed_at TIMESTAMP NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_status (status),
    INDEX idx_channel (channel),
    INDEX idx_transactions_customer_date (customer_id, transaction_date),
    INDEX idx_transactions_type_date (transaction_type, transaction_date),
    FOREIGN KEY (customer_id) REFERENCES customers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'Created table: transactions' AS Status;

-- =============================================================================
-- TABLE 5: CHARGE_CALCULATIONS - Results of charge calculations
-- =============================================================================

CREATE TABLE charge_calculations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL,
    rule_id BIGINT NOT NULL,
    
    -- Calculation details
    calculated_amount DECIMAL(10,2) NOT NULL,
    currency_code VARCHAR(3) DEFAULT 'INR',
    calculation_basis VARCHAR(500), -- Description of how charge was calculated
    
    -- Rule application context
    threshold_count_used INT DEFAULT 0, -- How many transactions counted for threshold
    period_start DATE,
    period_end DATE,
    
    -- Processing status
    status ENUM('CALCULATED', 'APPLIED', 'WAIVED', 'REVERSED') DEFAULT 'CALCULATED',
    applied_at TIMESTAMP NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_rule_id (rule_id),
    INDEX idx_status (status),
    INDEX idx_period (period_start, period_end),
    INDEX idx_charge_calc_period (period_start, period_end, status),
    FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    FOREIGN KEY (rule_id) REFERENCES charge_rules(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'Created table: charge_calculations' AS Status;

-- =============================================================================
-- TABLE 6: SETTLEMENT_REQUESTS - Aggregated charges for settlement
-- =============================================================================

CREATE TABLE settlement_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    settlement_id VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL,
    
    -- Settlement period
    period_from DATE NOT NULL,
    period_to DATE NOT NULL,
    
    -- Financial details
    total_charges DECIMAL(12,2) NOT NULL,
    currency_code VARCHAR(3) DEFAULT 'INR',
    number_of_transactions INT NOT NULL,
    
    -- Settlement instructions
    settlement_account VARCHAR(50) NOT NULL,
    settlement_method ENUM('DEBIT', 'INVOICE', 'ADJUSTMENT') DEFAULT 'DEBIT',
    
    -- Status tracking
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED') DEFAULT 'PENDING',
    processed_at TIMESTAMP NULL,
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    approved_by BIGINT,
    approved_at TIMESTAMP NULL,
    
    INDEX idx_settlement_id (settlement_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_period (period_from, period_to),
    INDEX idx_status (status),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'Created table: settlement_requests' AS Status;

-- =============================================================================
-- TABLE 7: SETTLEMENT_DETAILS - Individual charges in each settlement
-- =============================================================================

CREATE TABLE settlement_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    settlement_request_id BIGINT NOT NULL,
    charge_calculation_id BIGINT NOT NULL,
    
    -- Detail amounts (for auditing)
    charge_amount DECIMAL(10,2) NOT NULL,
    currency_code VARCHAR(3) DEFAULT 'INR',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_settlement_request_id (settlement_request_id),
    INDEX idx_charge_calculation_id (charge_calculation_id),
    FOREIGN KEY (settlement_request_id) REFERENCES settlement_requests(id),
    FOREIGN KEY (charge_calculation_id) REFERENCES charge_calculations(id),
    
    -- Prevent duplicate entries
    UNIQUE KEY unique_settlement_charge (settlement_request_id, charge_calculation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'Created table: settlement_details' AS Status;

-- =============================================================================
-- TABLE 8: AUDIT_LOG - Track all system changes for compliance
-- =============================================================================

CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type VARCHAR(50) NOT NULL, -- 'CHARGE_RULE', 'USER', 'TRANSACTION', etc.
    entity_id BIGINT NOT NULL,
    action ENUM('CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT') NOT NULL,
    
    -- Change tracking
    old_values JSON,
    new_values JSON,
    changes_summary TEXT,
    
    -- Context
    user_id BIGINT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'Created table: audit_log' AS Status;

-- =============================================================================
-- SECTION 3: VIEWS FOR REPORTING AND ANALYTICS
-- =============================================================================

-- View for active rules summary
CREATE OR REPLACE VIEW active_rules_view AS
SELECT 
    r.rule_code,
    r.rule_name,
    r.category,
    r.activity_type,
    r.fee_type,
    r.fee_value,
    r.currency_code,
    CONCAT(u.first_name, ' ', u.last_name) AS created_by_name,
    r.created_at,
    r.effective_from
FROM charge_rules r
JOIN users u ON r.created_by = u.id
WHERE r.status = 'ACTIVE'
    AND (r.effective_to IS NULL OR r.effective_to > CURRENT_TIMESTAMP);

SELECT 'Created view: active_rules_view' AS Status;

-- View for settlement summary
CREATE OR REPLACE VIEW settlement_summary_view AS
SELECT 
    sr.settlement_id,
    c.customer_code,
    COALESCE(CONCAT(c.first_name, ' ', c.last_name), c.company_name) AS customer_name,
    sr.period_from,
    sr.period_to,
    sr.total_charges,
    sr.number_of_transactions,
    sr.status,
    sr.created_at
FROM settlement_requests sr
JOIN customers c ON sr.customer_id = c.id;

SELECT 'Created view: settlement_summary_view' AS Status;

-- =====================================================================
-- SECTION 4: SAMPLE DATA - DEFAULT USERS
-- =====================================================================
-- Note: Password hashes are placeholders. They will be replaced by 
-- Spring Security BCrypt hashes when the application starts.
-- Default password for all users: "password123" (to be hashed by app)
-- =====================================================================

INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, is_active, created_at) VALUES
(1, 'admin', 'admin@chargemgmt.com', '$2a$10$placeholder.hash.for.admin.user.will.be.replaced', 'System', 'Administrator', 'ADMIN', true, NOW()),
(2, 'creator', 'creator@chargemgmt.com', '$2a$10$placeholder.hash.for.creator.user.will.be.replaced', 'Rule', 'Creator', 'RULE_CREATOR', true, NOW()),
(3, 'approver', 'approver@chargemgmt.com', '$2a$10$placeholder.hash.for.approver.user.will.be.replaced', 'Rule', 'Approver', 'RULE_APPROVER', true, NOW()),
(4, 'viewer', 'viewer@chargemgmt.com', '$2a$10$placeholder.hash.for.viewer.user.will.be.replaced', 'Rule', 'Viewer', 'RULE_VIEWER', true, NOW());

SELECT 'Inserted 4 default users (admin, creator, approver, viewer)' AS Status;

-- =====================================================================
-- SECTION 5: ALL 11 CHARGE RULES
-- Based on Business Requirements Document
-- =====================================================================

-- Rule 001: ATM Withdrawals from Parent Bank > 20/month = 2% charge
INSERT INTO charge_rules (
    rule_code, rule_name, category, activity_type, 
    conditions_json, fee_type, fee_value, 
    status, created_by, effective_from, 
    threshold_count, threshold_period
) VALUES (
    '001', 
    'Number of ATM Withdrawals from parent bank machine', 
    'RETAIL_BANKING', 
    'RANGE_BASED', 
    '{"transaction_type": "ATM_WITHDRAWAL_PARENT", "threshold": 20, "period": "MONTHLY", "description": "Charge 2% for ATM withdrawals exceeding 20 per month from parent bank ATMs"}',
    'PERCENTAGE', 
    2.0000, 
    'ACTIVE', 
    1, 
    NOW(), 
    20, 
    'MONTHLY'
);

-- Rule 002: Monthly Charges - Savings Account = ₹25/month
INSERT INTO charge_rules (
    rule_code, rule_name, category, activity_type, 
    conditions_json, fee_type, fee_value, 
    status, created_by, effective_from, 
    threshold_period
) VALUES (
    '002', 
    'Monthly Charges-Savings', 
    'RETAIL_BANKING', 
    'MONTHLY',
    '{"account_type": "SAVINGS", "frequency": "MONTHLY", "description": "Fixed monthly maintenance charge for savings accounts"}',
    'FLAT_AMOUNT', 
    25.0000, 
    'ACTIVE', 
    1, 
    NOW(), 
    'MONTHLY'
);

-- Rule 003: Bi-Monthly Charges - Corporate Current Account = 5% of monthly average
INSERT INTO charge_rules (
    rule_code, rule_name, category, activity_type, 
    conditions_json, fee_type, fee_value, 
    status, created_by, effective_from, 
    threshold_period
) VALUES (
    '003', 
    'Monthly Charges-Current', 
    'CORP_BANKING', 
    'MONTHLY',
    '{"account_type": "CURRENT", "frequency": "BI_MONTHLY", "calculation": "5% of monthly average", "description": "Bi-monthly charge calculated as 5% of monthly average balance"}',
    'PERCENTAGE', 
    5.0000, 
    'ACTIVE', 
    1, 
    NOW(), 
    'MONTHLY'
);

-- Rule 004: Statement Print Copy - On Demand = ₹50
INSERT INTO charge_rules (
    rule_code, rule_name, category, activity_type, 
    conditions_json, fee_type, fee_value, 
    status, created_by, effective_from
) VALUES (
    '004', 
    'Statement Print copy', 
    'RETAIL_BANKING', 
    'SPECIAL',
    '{"transaction_type": "STATEMENT_PRINT", "frequency": "ON_DEMAND", "description": "Per-request charge for printed account statements"}',
    'FLAT_AMOUNT', 
    50.0000, 
    'ACTIVE', 
    1, 
    NOW()
);

-- Rule 005: Duplicate Debit Card - On Demand = ₹150
INSERT INTO charge_rules (
    rule_code, rule_name, category, activity_type, 
    conditions_json, fee_type, fee_value, 
    status, created_by, effective_from
) VALUES (
    '005', 
    'Duplicate Debit Card', 
    'RETAIL_BANKING', 
    'SPECIAL',
    '{"transaction_type": "DUPLICATE_DEBIT_CARD", "frequency": "ON_DEMAND", "description": "Card replacement fee for lost or damaged debit cards"}',
    'FLAT_AMOUNT', 
    150.0000, 
    'ACTIVE', 
    1, 
    NOW()
);

-- Rule 006: Duplicate Credit Card - On Demand = ₹450
INSERT INTO charge_rules (
    rule_code, rule_name, category, activity_type, 
    conditions_json, fee_type, fee_value, 
    status, created_by, effective_from
) VALUES (
    '006', 
    'Duplicate Credit Card', 
    'RETAIL_BANKING', 
    'SPECIAL',
    '{"transaction_type": "DUPLICATE_CREDIT_CARD", "frequency": "ON_DEMAND", "description": "Card replacement fee for lost or damaged credit cards"}',
    'FLAT_AMOUNT', 
    450.0000, 
    'ACTIVE', 
    1, 
    NOW()
);

-- Rule 007: ATM Withdrawals from Other Bank > 5/month = 10% charge
INSERT INTO charge_rules (
    rule_code, rule_name, category, activity_type, 
    conditions_json, fee_type, fee_value, 
    status, created_by, effective_from, 
    threshold_count, threshold_period
) VALUES (
    '007', 
    'Number of ATM Withdrawals from another bank machine', 
    'RETAIL_BANKING', 
    'RANGE_BASED',
    '{"transaction_type": "ATM_WITHDRAWAL_OTHER", "threshold": 5, "period": "MONTHLY", "description": "Charge 10% for ATM withdrawals exceeding 5 per month from other bank ATMs"}',
    'PERCENTAGE', 
    10.0000, 
    'ACTIVE', 
    1, 
    NOW(), 
    5, 
    'MONTHLY'
);

-- Rule 008: Funds Transfer - Free up to 10/month
INSERT INTO charge_rules (
    rule_code, rule_name, category, activity_type, 
    conditions_json, fee_type, fee_value, 
    status, created_by, effective_from, 
    threshold_count, threshold_period
) VALUES (
    '008', 
    'Funds Transfer - Less than 10 in a Month', 
    'ALL', 
    'RANGE_BASED',
    '{"transaction_type": "FUNDS_TRANSFER", "threshold": 10, "period": "MONTHLY", "description": "No charge for first 10 fund transfers per month"}',
    'FLAT_AMOUNT', 
    0.0000, 
    'ACTIVE', 
    1, 
    NOW(), 
    10, 
    'MONTHLY'
);

-- Rule 009: Funds Transfer - 11 to 30/month = ₹100
INSERT INTO charge_rules (
    rule_code, rule_name, category, activity_type, 
    conditions_json, fee_type, fee_value, 
    status, created_by, effective_from, 
    threshold_period
) VALUES (
    '009', 
    'Funds Transfer - 11 to 30 in a Month', 
    'ALL', 
    'RANGE_BASED',
    '{"transaction_type": "FUNDS_TRANSFER", "min_count": 11, "max_count": 30, "period": "MONTHLY", "description": "Flat ₹100 charge for fund transfers between 11-30 per month"}',
    'FLAT_AMOUNT', 
    100.0000, 
    'ACTIVE', 
    1, 
    NOW(), 
    'MONTHLY'
);

-- Rule 010: Funds Transfer - 31 to 50/month = ₹150
INSERT INTO charge_rules (
    rule_code, rule_name, category, activity_type, 
    conditions_json, fee_type, fee_value, 
    status, created_by, effective_from, 
    threshold_period
) VALUES (
    '010', 
    'Funds Transfer - 31 to 50 in a Month', 
    'ALL', 
    'RANGE_BASED',
    '{"transaction_type": "FUNDS_TRANSFER", "min_count": 31, "max_count": 50, "period": "MONTHLY", "description": "Flat ₹150 charge for fund transfers between 31-50 per month"}',
    'FLAT_AMOUNT', 
    150.0000, 
    'ACTIVE', 
    1, 
    NOW(), 
    'MONTHLY'
);

-- Rule 011: Funds Transfer - More than 51/month = ₹300
INSERT INTO charge_rules (
    rule_code, rule_name, category, activity_type, 
    conditions_json, fee_type, fee_value, 
    status, created_by, effective_from, 
    threshold_period
) VALUES (
    '011', 
    'Funds Transfer - More than 51', 
    'ALL', 
    'RANGE_BASED',
    '{"transaction_type": "FUNDS_TRANSFER", "min_count": 51, "period": "MONTHLY", "description": "Flat ₹300 charge for fund transfers exceeding 51 per month"}',
    'FLAT_AMOUNT', 
    300.0000, 
    'ACTIVE', 
    1, 
    NOW(), 
    'MONTHLY'
);

SELECT 'Inserted all 11 charge rules (001-011)' AS Status;

-- =====================================================================
-- SECTION 6: SAMPLE CUSTOMERS
-- =====================================================================

INSERT INTO customers (
    customer_code, customer_type, first_name, last_name, 
    email, phone, status, registration_date
) VALUES
('CUST001', 'RETAIL', 'John', 'Doe', 'john.doe@email.com', '9876543210', 'ACTIVE', '2024-01-15'),
('CUST002', 'RETAIL', 'Jane', 'Smith', 'jane.smith@email.com', '9876543211', 'ACTIVE', '2024-02-20'),
('CUST003', 'CORPORATE', NULL, NULL, 'contact@techcorp.com', '9876543212', 'ACTIVE', '2024-03-01');

-- Update corporate customer with company name
UPDATE customers 
SET company_name = 'TechCorp Solutions' 
WHERE customer_code = 'CUST003';

SELECT 'Inserted 3 sample customers (2 retail, 1 corporate)' AS Status;

-- =====================================================================
-- SECTION 7: VERIFICATION QUERIES
-- =====================================================================

SELECT '========================================' AS '';
SELECT 'DATABASE SETUP VERIFICATION' AS '';
SELECT '========================================' AS '';

-- Count summary
SELECT 
    'Tables Created' AS Metric,
    COUNT(*) AS Count 
FROM information_schema.tables 
WHERE table_schema = 'charge_management_dev' 
    AND table_type = 'BASE TABLE'
UNION ALL
SELECT 
    'Views Created',
    COUNT(*) 
FROM information_schema.views 
WHERE table_schema = 'charge_management_dev'
UNION ALL
SELECT 
    'Users Inserted',
    COUNT(*) 
FROM users
UNION ALL
SELECT 
    'Rules Inserted',
    COUNT(*) 
FROM charge_rules
UNION ALL
SELECT 
    'Customers Inserted',
    COUNT(*) 
FROM customers;

SELECT '========================================' AS '';
SELECT 'USER ACCOUNTS' AS '';
SELECT '========================================' AS '';

SELECT 
    id,
    username,
    email,
    role,
    CASE WHEN is_active THEN '✓ Active' ELSE '✗ Inactive' END AS status
FROM users
ORDER BY id;

SELECT '========================================' AS '';
SELECT 'CHARGE RULES SUMMARY' AS '';
SELECT '========================================' AS '';

SELECT 
    rule_code,
    rule_name,
    category,
    activity_type,
    CONCAT(
        CASE 
            WHEN fee_type = 'PERCENTAGE' THEN CONCAT(fee_value, '%')
            WHEN fee_type = 'FLAT_AMOUNT' THEN CONCAT('₹', fee_value)
            ELSE CONCAT(fee_value)
        END
    ) AS fee,
    status
FROM charge_rules
ORDER BY rule_code;

SELECT '========================================' AS '';
SELECT 'CUSTOMER ACCOUNTS' AS '';
SELECT '========================================' AS '';

SELECT 
    customer_code,
    customer_type,
    COALESCE(CONCAT(first_name, ' ', last_name), company_name) AS name,
    email,
    phone,
    status,
    registration_date
FROM customers
ORDER BY customer_code;

SELECT '========================================' AS '';
SELECT 'ACTIVE RULES VIEW' AS '';
SELECT '========================================' AS '';

SELECT 
    rule_code,
    rule_name,
    category,
    CONCAT(
        CASE 
            WHEN fee_type = 'PERCENTAGE' THEN CONCAT(fee_value, '%')
            WHEN fee_type = 'FLAT_AMOUNT' THEN CONCAT('₹', fee_value)
            ELSE CONCAT(fee_value)
        END
    ) AS fee,
    created_by_name
FROM active_rules_view
ORDER BY rule_code;

-- =====================================================================
-- FINAL STATUS
-- =====================================================================

SELECT '========================================' AS '';
SELECT '✓ DATABASE SETUP COMPLETED SUCCESSFULLY!' AS '';
SELECT '========================================' AS '';
SELECT '' AS '';
SELECT 'Database: charge_management_dev' AS 'Setup Summary';
SELECT 'Tables: 8' AS '';
SELECT 'Views: 2' AS '';
SELECT 'Users: 4 (admin, creator, approver, viewer)' AS '';
SELECT 'Rules: 11 (001-011)' AS '';
SELECT 'Customers: 3 (2 retail, 1 corporate)' AS '';
SELECT '' AS '';
SELECT 'Next Steps:' AS '';
SELECT '1. Update application.properties with database credentials' AS '';
SELECT '2. Run Spring Boot application to initialize BCrypt password hashes' AS '';
SELECT '3. Default login: username=admin, password=password123 (or as configured in app)' AS '';
SELECT '4. All tables are indexed and optimized for performance' AS '';
SELECT '========================================' AS '';

COMMIT;

-- =====================================================================
-- END OF SCRIPT
-- =====================================================================
