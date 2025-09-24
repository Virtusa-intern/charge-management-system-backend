package com.bank.charge_management_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
public class ChargeManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChargeManagementSystemApplication.class, args);
    }
    
    /**
     * Health check endpoint to verify application startup
     */
    @GetMapping("/api/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Charge Management System");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        return response;
    }
    
    /**
     * Welcome endpoint for initial testing
     */
    @GetMapping("/api/welcome")
    public Map<String, String> welcome() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to Charge Management System");
        response.put("description", "Centralized charge processing for banking operations");
        return response;
    }
}