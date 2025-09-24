// src/test/java/com/bank/chargemgmt/ChargeManagementApplicationTests.java
package com.bank.charge_management_system;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ChargeManagementSystemApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Test 1: Verify Spring Boot application context loads successfully
     */
    @Test
    void contextLoads() {
        // This test will fail if the application context cannot be loaded
        assertNotNull(restTemplate, "RestTemplate should be autowired");
    }

    /**
     * Test 2: Verify health check endpoint is accessible and returns correct response
     */
    @Test
    void healthCheckEndpoint() {
        String url = "http://localhost:" + port + "/charge-mgmt/api/health";
        
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        
        // Verify response status
        assertEquals(HttpStatus.OK, response.getStatusCode(), 
            "Health check should return HTTP 200");
        
        // Verify response body
        Map<String, Object> body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals("UP", body.get("status"), "Status should be UP");
        assertEquals("Charge Management System", body.get("service"), 
            "Service name should match");
        assertEquals("1.0.0", body.get("version"), "Version should match");
        assertNotNull(body.get("timestamp"), "Timestamp should be present");
        
        System.out.println("✅ Health check endpoint test passed");
        System.out.println("Response: " + body);
    }

    /**
     * Test 3: Verify welcome endpoint is accessible
     */
    @Test
    void welcomeEndpoint() {
        String url = "http://localhost:" + port + "/charge-mgmt/api/welcome";
        
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        
        // Verify response status
        assertEquals(HttpStatus.OK, response.getStatusCode(),
            "Welcome endpoint should return HTTP 200");
        
        // Verify response content
        Map<String, String> body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertTrue(body.get("message").contains("Welcome"), 
            "Message should contain welcome text");
        assertNotNull(body.get("description"), "Description should be present");
        
        System.out.println("✅ Welcome endpoint test passed");
        System.out.println("Response: " + body);
    }

    /**
     * Test 4: Verify application properties are loaded correctly
     */
    @Test
    void applicationPropertiesLoaded() {
        // Test will pass if application starts without property loading errors
        String url = "http://localhost:" + port + "/charge-mgmt/api/health";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertNotNull(response, "Application should respond to requests");
        assertEquals(HttpStatus.OK, response.getStatusCode(), 
            "Application should be healthy");
        
        System.out.println("✅ Application properties loaded successfully");
    }
}
