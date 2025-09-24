package com.bank.charge_management_system.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/database")
public class DatabaseTestController {

    @Autowired
    private DataSource dataSource;

    /**
     * Test database connectivity and return connection details
     */
    @GetMapping("/test")
    public Map<String, Object> testDatabaseConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // Basic connection info
            result.put("status", "SUCCESS");
            result.put("database", connection.getMetaData().getDatabaseProductName());
            result.put("version", connection.getMetaData().getDatabaseProductVersion());
            result.put("url", connection.getMetaData().getURL());
            result.put("username", connection.getMetaData().getUserName());
            result.put("timestamp", LocalDateTime.now());
            result.put("message", "Database connection successful");
            
            // Test table existence and count records
            Map<String, Integer> tableCounts = new HashMap<>();
            String[] tables = {"users", "charge_rules", "customers", "transactions", 
                             "charge_calculations", "settlement_requests"};
            
            try (Statement stmt = connection.createStatement()) {
                for (String table : tables) {
                    try {
                        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
                        if (rs.next()) {
                            tableCounts.put(table, rs.getInt(1));
                        }
                        rs.close();
                    } catch (Exception e) {
                        tableCounts.put(table + "_error", -1);
                    }
                }
            }
            
            result.put("table_counts", tableCounts);
            
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
            result.put("message", "Database connection failed");
            result.put("timestamp", LocalDateTime.now());
        }
        
        return result;
    }

    /**
     * Get detailed database schema information
     */
    @GetMapping("/schema")
    public Map<String, Object> getDatabaseSchema() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            result.put("status", "SUCCESS");
            
            // Get all tables in the database
            String query = """
                SELECT TABLE_NAME, TABLE_ROWS, CREATE_TIME 
                FROM INFORMATION_SCHEMA.TABLES 
                WHERE TABLE_SCHEMA = DATABASE()
                ORDER BY TABLE_NAME
                """;
                
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                
                Map<String, Object> tables = new HashMap<>();
                while (rs.next()) {
                    Map<String, Object> tableInfo = new HashMap<>();
                    tableInfo.put("row_count", rs.getLong("TABLE_ROWS"));
                    tableInfo.put("created", rs.getTimestamp("CREATE_TIME"));
                    tables.put(rs.getString("TABLE_NAME"), tableInfo);
                }
                result.put("tables", tables);
            }
            
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * Test sample data queries
     */
    @GetMapping("/sample-data")
    public Map<String, Object> testSampleData() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            result.put("status", "SUCCESS");
            Map<String, Object> sampleData = new HashMap<>();
            
            // Test users
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT username, role FROM users LIMIT 5");
                Map<String, String> users = new HashMap<>();
                while (rs.next()) {
                    users.put(rs.getString("username"), rs.getString("role"));
                }
                sampleData.put("users", users);
                rs.close();
            }
            
            // Test charge rules
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT rule_code, rule_name, status FROM charge_rules LIMIT 5");
                Map<String, Object> rules = new HashMap<>();
                while (rs.next()) {
                    Map<String, String> ruleInfo = new HashMap<>();
                    ruleInfo.put("name", rs.getString("rule_name"));
                    ruleInfo.put("status", rs.getString("status"));
                    rules.put(rs.getString("rule_code"), ruleInfo);
                }
                sampleData.put("charge_rules", rules);
                rs.close();
            }
            
            // Test customers
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT customer_code, customer_type, first_name, company_name FROM customers LIMIT 5");
                Map<String, Object> customers = new HashMap<>();
                while (rs.next()) {
                    Map<String, Object> customerInfo = new HashMap<>();
                    customerInfo.put("type", rs.getString("customer_type"));
                    customerInfo.put("name", rs.getString("first_name") != null ? 
                        rs.getString("first_name") : rs.getString("company_name"));
                    customers.put(rs.getString("customer_code"), customerInfo);
                }
                sampleData.put("customers", customers);
                rs.close();
            }
            
            result.put("sample_data", sampleData);
            result.put("message", "Sample data retrieved successfully");
            
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
            result.put("message", "Failed to retrieve sample data");
        }
        
        return result;
    }
}