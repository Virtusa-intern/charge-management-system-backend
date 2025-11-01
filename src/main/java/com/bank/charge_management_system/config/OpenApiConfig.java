package com.bank.charge_management_system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration
 * 
 * This configuration provides interactive API documentation for the Charge
 * Management System.
 * Access the Swagger UI at: http://localhost:8080/swagger-ui.html
 * Access the OpenAPI JSON at: http://localhost:8080/v3/api-docs
 * 
 * Features:
 * - Interactive API testing
 * - JWT authentication support
 * - Request/Response examples
 * - Schema documentation
 * 
 * @author Virtusa Intern
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:Charge Management System}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Configures the OpenAPI documentation for the application
     * 
     * @return OpenAPI configuration with security, info, and server details
     */
    @Bean
    public OpenAPI customOpenAPI() {
        // Security scheme for JWT Bearer token
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("Enter JWT Bearer token in the format: Bearer {token}");

        // Security requirement to apply JWT to all endpoints
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");

        return new OpenAPI()
                // API Information
                .info(new Info()
                        .title("Charge Management System API")
                        .version("1.0.0")
                        .description(
                                """
                                        # Charge Management System API Documentation

                                        ## Overview
                                        A comprehensive RESTful API for managing banking charges, rules, settlements, and transactions.

                                        ## Features
                                        - **Authentication**: JWT-based secure authentication
                                        - **Role-Based Access Control**: 4 user roles (ADMIN, RULE_CREATOR, RULE_APPROVER, RULE_VIEWER)
                                        - **Charge Rule Management**: Create, update, approve, and manage charge rules
                                        - **Charge Calculation**: Calculate charges based on transaction types and rules
                                        - **Settlement Management**: Create and approve settlement requests
                                        - **Transaction History**: Track and query all transactions
                                        - **User Management**: Admin portal for user CRUD operations
                                        - **Reports & Analytics**: Generate reports and export data

                                        ## Authentication
                                        1. Call `/api/auth/login` with username and password
                                        2. Copy the JWT token from the response
                                        3. Click the **Authorize** button (🔒) at the top right
                                        4. Enter: `Bearer {your-token-here}`
                                        5. Click **Authorize** and close the dialog
                                        6. Now you can test secured endpoints!

                                        ## User Roles
                                        - **ADMIN**: Full system access, user management
                                        - **RULE_CREATOR**: Create and edit charge rules, create settlements
                                        - **RULE_APPROVER**: Approve/reject rules and settlements
                                        - **RULE_VIEWER**: Read-only access to all features

                                        ## Default Test Users
                                        - **Admin**: username: `admin`, password: `admin123`
                                        - **Rule Creator**: username: `rule_creator`, password: `creator123`
                                        - **Rule Approver**: username: `rule_approver`, password: `approver123`
                                        - **Viewer**: username: `viewer`, password: `viewer123`

                                        ## Response Codes
                                        - **200 OK**: Successful request
                                        - **201 Created**: Resource created successfully
                                        - **400 Bad Request**: Validation error or invalid input
                                        - **401 Unauthorized**: Authentication required or token invalid
                                        - **403 Forbidden**: Insufficient permissions for the operation
                                        - **404 Not Found**: Resource not found
                                        - **500 Internal Server Error**: Server error

                                        ## Support
                                        For questions or issues, contact the development team.
                                        """)
                        .contact(new Contact()
                                .name("Virtusa Internship Team")
                                .email("virtusa.intern@example.com")
                                .url("https://github.com/Virtusa-intern/Charge-management-system"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))

                // Server Configuration
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort + "/charge-mgmt")
                                .description("Development Server"),
                        new Server()
                                .url("http://localhost:8080/charge-mgmt")
                                .description("Default Server")))

                // Security Configuration
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}
