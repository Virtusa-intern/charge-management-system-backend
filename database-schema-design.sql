-- Step 4: Database Schema Design for Charge Management System
-- This script creates all necessary tables for the banking charge management system

-- Create database (run this first)
CREATE DATABASE IF NOT EXISTS charge_management_dev;
USE charge_management_dev;

-- =============================================================================
-- 1. USERS TABLE - For authentication and role management
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
);

-- =============================================================================
-- 2. CHARGE_RULES TABLE - Core table for storing all charging rules
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
);

-- =============================================================================
-- 3. CUSTOMERS TABLE - Customer information for transaction processing
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
);

-- =============================================================================
-- 4. TRANSACTIONS TABLE - All banking transactions that may incur charges
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
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- =============================================================================
-- 5. CHARGE_CALCULATIONS TABLE - Results of charge calculations
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
    FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    FOREIGN KEY (rule_id) REFERENCES charge_rules(id)
);

-- =============================================================================
-- 6. SETTLEMENT_REQUESTS TABLE - Aggregated charges for settlement
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
);

-- =============================================================================
-- 7. SETTLEMENT_DETAILS TABLE - Individual charges in each settlement
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
);

-- =============================================================================
-- 8. AUDIT_LOG TABLE - Track all system changes for compliance
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
);

-- =============================================================================
-- SAMPLE DATA FOR TESTING
-- =============================================================================

-- Insert default admin user (password: admin123 - hash would be generated by Spring Security)
INSERT INTO users (username, email, password_hash, first_name, last_name, role) VALUES 
('admin', 'admin@bank.com', '$2a$10$example.hash.here', 'System', 'Administrator', 'ADMIN'),
('rule_creator', 'creator@bank.com', '$2a$10$example.hash.here', 'Rule', 'Creator', 'RULE_CREATOR'),
('approver', 'approver@bank.com', '$2a$10$example.hash.here', 'Rule', 'Approver', 'RULE_APPROVER');

-- Insert sample charge rules based on your requirements
INSERT INTO charge_rules (
    rule_code, rule_name, category, activity_type, conditions_json, 
    fee_type, fee_value, status, created_by
) VALUES 
('001', 'ATM Withdrawals from parent bank > 20/month', 'RETAIL_BANKING', 'RANGE_BASED', 
    '{"transaction_type": "ATM_WITHDRAWAL_PARENT", "threshold": 20, "period": "MONTHLY"}', 
    'PERCENTAGE', 2.0000, 'ACTIVE', 1),

('002', 'Monthly Charges - Savings Account', 'RETAIL_BANKING', 'MONTHLY', 
    '{"account_type": "SAVINGS", "frequency": "MONTHLY"}', 
    'FLAT_AMOUNT', 25.0000, 'ACTIVE', 1),

('003', 'Monthly Charges - Current Account', 'CORP_BANKING', 'MONTHLY', 
    '{"account_type": "CURRENT", "frequency": "BI_MONTHLY", "calculation": "5% of monthly average"}', 
    'PERCENTAGE', 5.0000, 'ACTIVE', 1),

('008', 'Funds Transfer - Free up to 10/month', 'ALL', 'RANGE_BASED', 
    '{"transaction_type": "FUNDS_TRANSFER", "threshold": 10, "period": "MONTHLY"}', 
    'FLAT_AMOUNT', 0.0000, 'ACTIVE', 1),

('009', 'Funds Transfer - 11 to 30/month', 'ALL', 'RANGE_BASED', 
    '{"transaction_type": "FUNDS_TRANSFER", "min_count": 11, "max_count": 30, "period": "MONTHLY"}', 
    'FLAT_AMOUNT', 100.0000, 'ACTIVE', 1);

-- Insert sample customers (CORRECTED VERSION)
-- Corrected INSERT statement
-- INSERT INTO customers (customer_code, customer_type, first_name, last_name, email, phone, registration_date) VALUES 
-- ('CUST001', 'RETAIL', 'John', 'Doe', 'john.doe@email.com', '9876543210', '2024-01-15'),
-- ('CUST002', 'RETAIL', 'Jane', 'Smith', 'jane.smith@email.com', '9876543211', '2024-02-20');
-- -- This should work - only specify the essential columns
INSERT INTO customers (customer_code, customer_type, company_name, email, phone, registration_date) VALUES 
('CORP001', 'CORPORATE', 'TechCorp Solutions', 'contact@techcorp.com', '9876543212', '2024-03-01');
-- Alternative: Insert corporate customer with proper company name in company_name field
INSERT INTO customers (customer_code, customer_type, first_name, last_name, company_name, email, phone, registration_date) VALUES 
('CORP002', 'CORPORATE', NULL, NULL, 'TechCorp Solutions', 'contact@techcorp.com', '9876543213', '2024-03-01');

-- =============================================================================
-- VIEWS FOR REPORTING AND ANALYTICS
-- =============================================================================

-- View for active rules summary
CREATE VIEW active_rules_view AS
SELECT 
    r.rule_code,
    r.rule_name,
    r.category,
    r.activity_type,
    r.fee_type,
    r.fee_value,
    r.currency_code,
    u.first_name AS created_by_name,
    r.created_at,
    r.effective_from
FROM charge_rules r
JOIN users u ON r.created_by = u.id
WHERE r.status = 'ACTIVE'
    AND (r.effective_to IS NULL OR r.effective_to > CURRENT_TIMESTAMP);

-- View for settlement summary
CREATE VIEW settlement_summary_view AS
SELECT 
    sr.settlement_id,
    c.customer_code,
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
    sr.period_from,
    sr.period_to,
    sr.total_charges,
    sr.number_of_transactions,
    sr.status,
    sr.created_at
FROM settlement_requests sr
JOIN customers c ON sr.customer_id = c.id;

-- =============================================================================
-- INDEXES FOR PERFORMANCE (Additional)
-- =============================================================================

-- Composite indexes for common queries
CREATE INDEX idx_transactions_customer_date ON transactions(customer_id, transaction_date);
CREATE INDEX idx_transactions_type_date ON transactions(transaction_type, transaction_date);
CREATE INDEX idx_charge_calc_period ON charge_calculations(period_start, period_end, status);

-- Full-text search indexes for rule names and descriptions
-- ALTER TABLE charge_rules ADD FULLTEXT(rule_name, conditions_json);

COMMIT;