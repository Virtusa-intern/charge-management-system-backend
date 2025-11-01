# Charge Management System - Backend

## 📋 Overview

A robust Spring Boot-based backend system for managing banking charges, rules, and settlements. This system provides comprehensive REST APIs for charge calculation, rule management, user authentication, and settlement processing.

## 🚀 Features

- **Charge Calculation Engine**: Dynamic calculation of charges based on configurable rules
- **Rule Management**: CRUD operations for charge rules with category-based filtering
- **User Management**: Role-based access control (ADMIN, MAKER, CHECKER, VIEWER)
- **Settlement Processing**: Automated settlement request handling and approval workflow
- **Transaction History**: Complete transaction tracking and charge calculations
- **Reports & Analytics**: Comprehensive statistics and performance metrics
- **Swagger Documentation**: Interactive API documentation with detailed schemas
- **Security**: JWT-based authentication with role-based authorization

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.5.5
- **Java Version**: 21
- **Database**: MySQL 8.0
- **Security**: Spring Security with JWT
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito

## 📦 Prerequisites

- Java 21 or higher
- Maven 3.9+
- MySQL 8.0
- Git

## ⚙️ Configuration

### Database Setup

1. Create MySQL database:
```sql
CREATE DATABASE charge_management;
```

2. Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/charge_management
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Application Profiles

The system supports multiple profiles:
- **dev**: Development environment (default)
- **prod**: Production environment

Activate profile: `spring.profiles.active=dev`

## 🏃 Running the Application

### Option 1: Using Maven Wrapper (Recommended)

```bash
# Windows
.\mvnw spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### Option 2: Using Maven

```bash
mvn spring-boot:run
```

### Option 3: Skip Tests

```bash
.\mvnw spring-boot:run -DskipTests
```

The application will start on `http://localhost:8080/charge-mgmt`

## 📚 API Documentation

Access Swagger UI at: **http://localhost:8080/charge-mgmt/swagger-ui.html**

### Available Endpoints

#### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `GET /api/auth/me` - Get current user info

#### Charge Calculation
- `POST /api/charges/calculate` - Calculate charges for a transaction
- `GET /api/charges/statistics` - Get charge statistics
- `GET /api/charges/{id}` - Get charge calculation by ID
- `GET /api/charges/transaction/{id}` - Get charges for a transaction

#### Charge Rules
- `GET /api/rules` - Get all rules (with filters)
- `POST /api/rules` - Create new rule (ADMIN/MAKER)
- `PUT /api/rules/{id}` - Update rule (ADMIN/MAKER)
- `DELETE /api/rules/{id}` - Delete rule (ADMIN)
- `GET /api/rules/statistics` - Get rule performance statistics

#### User Management
- `GET /api/users` - Get all users (ADMIN)
- `POST /api/users` - Create new user (ADMIN)
- `PUT /api/users/{id}` - Update user (ADMIN)
- `GET /api/users/statistics` - Get user statistics

#### Settlement Requests
- `GET /api/settlements` - Get all settlement requests
- `POST /api/settlements` - Create settlement request
- `PUT /api/settlements/{id}/approve` - Approve settlement (ADMIN/CHECKER)
- `PUT /api/settlements/{id}/reject` - Reject settlement (ADMIN/CHECKER)

#### Customers
- `GET /api/customers` - Get all customers
- `GET /api/customers/{id}` - Get customer by ID

## 🔐 Default Users

The system initializes with default users:

| Username | Password | Role | Description |
|----------|----------|------|-------------|
| admin | admin123 | ADMIN | Full system access |
| maker | maker123 | MAKER | Create/edit rules |
| checker | checker123 | CHECKER | Approve/reject requests |
| viewer | viewer123 | VIEWER | Read-only access |

**⚠️ IMPORTANT**: Change default passwords in production!

## 👥 Default Customers

Sample customers are automatically created:

| Customer Code | Name | Type | Status |
|--------------|------|------|--------|
| CUST001 | Rajesh Kumar | RETAIL | ACTIVE |
| CUST002 | Priya Sharma | RETAIL | ACTIVE |
| CUST003 | Amit Patel | RETAIL | ACTIVE |
| CORP001 | TechCorp Solutions | CORPORATE | ACTIVE |
| CORP002 | Global Industries | CORPORATE | ACTIVE |

## 📊 Charge Rules

The system includes 11 pre-configured charge rules:

### Retail Banking Rules
- **Rule 001**: Minimum Balance Charge (₹500 below ₹10,000)
- **Rule 002**: Free Transaction Limit (₹10 per transaction after 5 free)
- **Rule 003**: ATM Withdrawal Fee (₹20 per withdrawal after 3 free)
- **Rule 004**: Statement Print (₹50 per statement)
- **Rule 005**: Duplicate Debit Card (₹150)
- **Rule 006**: Duplicate Credit Card (₹450)
- **Rule 007**: Cheque Book Issuance (₹100 per book)
- **Rule 008**: Cheque Return Charge (₹300 per return)
- **Rule 009**: Account Closure Fee (₹500)

### Corporate Banking Rules
- **Rule 010**: Corporate Service Fee (₹5,000/month)
- **Rule 011**: Bulk Transaction Fee (1% above ₹10,00,000)

## 🧪 Testing

### Run All Tests
```bash
.\mvnw test
```

### Run Specific Test Class
```bash
.\mvnw test -Dtest=ChargeCalculationControllerTest
```

### Generate Test Coverage Report
```bash
.\mvnw clean test jacoco:report
```

View coverage report: `target/site/jacoco/index.html`

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/bank/charge_management_system/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA entities
│   │   ├── repository/      # Data repositories
│   │   ├── service/         # Business logic
│   │   ├── security/        # Security configuration
│   │   └── util/            # Utility classes
│   └── resources/
│       ├── application.properties
│       ├── application-dev.properties
│       └── application-prod.properties
└── test/
    └── java/com/bank/charge_management_system/
        └── controller/      # Controller tests
```

## 🔧 Common Issues & Solutions

### Issue 1: Port Already in Use
```bash
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process (Windows)
taskkill /PID <PID> /F
```

### Issue 2: Database Connection Failed
- Verify MySQL is running
- Check database credentials in `application.properties`
- Ensure database `charge_management` exists

### Issue 3: Tests Failing
```bash
# Skip tests during build
.\mvnw clean install -DskipTests
```

## 🌐 Environment Variables

Configure these for production:

```properties
# Database
DB_HOST=your_database_host
DB_PORT=3306
DB_NAME=charge_management
DB_USER=your_username
DB_PASS=your_password

# JWT
JWT_SECRET=your_secret_key_here
JWT_EXPIRATION=86400000

# Server
SERVER_PORT=8080
```

## 📈 Performance Optimization

- **Connection Pooling**: HikariCP configured for optimal performance
- **JPA Query Optimization**: Indexed database columns
- **Caching**: Consider adding Redis for frequently accessed data
- **Logging**: Configurable log levels via `application.properties`

## 🔒 Security Best Practices

1. **Change Default Passwords**: Update all default user passwords
2. **Use HTTPS**: Configure SSL certificates for production
3. **Environment Variables**: Store sensitive data in environment variables
4. **Rate Limiting**: Implement API rate limiting
5. **Input Validation**: All endpoints validate input data
6. **SQL Injection Protection**: Parameterized queries used throughout

## 📝 API Request Examples

### Login
```bash
curl -X POST http://localhost:8080/charge-mgmt/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### Calculate Charge
```bash
curl -X POST http://localhost:8080/charge-mgmt/api/charges/calculate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "customerId": 1,
    "transactionType": "STATEMENT_PRINT",
    "amount": 100,
    "currency": "INR"
  }'
```

### Get Statistics
```bash
curl -X GET http://localhost:8080/charge-mgmt/api/charges/statistics \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🤝 Contributing

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Commit changes: `git commit -am 'Add new feature'`
3. Push to branch: `git push origin feature/your-feature`
4. Submit a pull request

## 📞 Support

For issues or questions:
- Check Swagger documentation: `/swagger-ui.html`
- Review application logs: `logs/charge-management.log`
- Contact development team

## 📄 License

Proprietary - Virtusa Internship Project

---

**Version**: 1.0.0  
**Last Updated**: November 2025  
**Maintained by**: Virtusa Development Team
