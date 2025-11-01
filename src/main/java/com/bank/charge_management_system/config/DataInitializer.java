package com.bank.charge_management_system.config;

import com.bank.charge_management_system.entity.Customer;
import com.bank.charge_management_system.entity.User;
import com.bank.charge_management_system.repository.CustomerRepository;
import com.bank.charge_management_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Data Initializer - Runs on application startup
 * Creates default users with properly hashed passwords
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 DataInitializer: Checking default users...");

        // Create default admin if not exists
        if (!userRepository.existsByUsername("admin")) {
            createDefaultUser(
                    "admin",
                    "admin@chargemgmt.com",
                    "admin123", // Plain password - will be hashed
                    "System",
                    "Administrator",
                    User.Role.ADMIN);
            System.out.println("✅ Created default ADMIN user (username: admin, password: admin123)");
        }

        // Create default rule creator if not exists
        if (!userRepository.existsByUsername("creator")) {
            createDefaultUser(
                    "creator",
                    "creator@chargemgmt.com",
                    "creator123",
                    "Rule",
                    "Creator",
                    User.Role.RULE_CREATOR);
            System.out.println("✅ Created default RULE_CREATOR user (username: creator, password: creator123)");
        }

        // Create default rule approver if not exists
        if (!userRepository.existsByUsername("approver")) {
            createDefaultUser(
                    "approver",
                    "approver@chargemgmt.com",
                    "approver123",
                    "Rule",
                    "Approver",
                    User.Role.RULE_APPROVER);
            System.out.println("✅ Created default RULE_APPROVER user (username: approver, password: approver123)");
        }

        // Create default rule viewer if not exists
        if (!userRepository.existsByUsername("viewer")) {
            createDefaultUser(
                    "viewer",
                    "viewer@chargemgmt.com",
                    "viewer123",
                    "Rule",
                    "Viewer",
                    User.Role.RULE_VIEWER);
            System.out.println("✅ Created default RULE_VIEWER user (username: viewer, password: viewer123)");
        }

        // Create default customers for testing
        createDefaultCustomers();

        System.out.println("🎉 DataInitializer: Initialization complete!");
    }

    /**
     * Create default customers for testing
     */
    private void createDefaultCustomers() {
        if (customerRepository.count() == 0) {
            System.out.println("🏦 Creating default customers...");

            // RETAIL Customer 1
            createDefaultCustomer(
                    "CUST001",
                    Customer.CustomerType.RETAIL,
                    "Rajesh",
                    "Kumar",
                    null,
                    "rajesh.kumar@email.com",
                    "9876543210");

            // RETAIL Customer 2
            createDefaultCustomer(
                    "CUST002",
                    Customer.CustomerType.RETAIL,
                    "Priya",
                    "Sharma",
                    null,
                    "priya.sharma@email.com",
                    "9876543211");

            // RETAIL Customer 3
            createDefaultCustomer(
                    "CUST003",
                    Customer.CustomerType.RETAIL,
                    "Amit",
                    "Patel",
                    null,
                    "amit.patel@email.com",
                    "9876543212");

            // CORPORATE Customer 1
            createDefaultCustomer(
                    "CORP001",
                    Customer.CustomerType.CORPORATE,
                    null,
                    null,
                    "TechCorp Solutions Pvt Ltd",
                    "accounts@techcorp.com",
                    "1234567890");

            // CORPORATE Customer 2
            createDefaultCustomer(
                    "CORP002",
                    Customer.CustomerType.CORPORATE,
                    null,
                    null,
                    "Global Industries Ltd",
                    "finance@globalind.com",
                    "1234567891");

            System.out.println("✅ Created 5 default customers (3 RETAIL + 2 CORPORATE)");
        } else {
            System.out.println("ℹ️ Customers already exist, skipping customer initialization");
        }
    }

    /**
     * Helper method to create a customer
     */
    private void createDefaultCustomer(String customerCode, Customer.CustomerType type,
            String firstName, String lastName, String companyName,
            String email, String phone) {
        Customer customer = new Customer();
        customer.setCustomerCode(customerCode);
        customer.setCustomerType(type);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setCompanyName(companyName);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setStatus(Customer.Status.ACTIVE);
        customer.setRegistrationDate(LocalDate.now());

        customerRepository.save(customer);
    }

    /**
     * Helper method to create a user with hashed password
     */
    private void createDefaultUser(String username, String email, String plainPassword,
            String firstName, String lastName, User.Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);

        // ⭐ MAGIC HAPPENS HERE: BCrypt password hashing
        user.setPasswordHash(passwordEncoder.encode(plainPassword));

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setIsActive(true);
        user.setCreatedBy(1L); // System user

        userRepository.save(user);
    }
}