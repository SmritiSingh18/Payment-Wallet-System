# Payment & Wallet System

A secure backend payment and wallet system built using Java and Spring Boot.

The project simulates a wallet-based payment system where users can register, authenticate using JWT, add money to their wallets, transfer money between wallets, and view transaction history.

The system focuses on important backend concepts such as **idempotency, concurrency control, double-spend prevention, audit logging, validation, exception handling, API documentation, containerization, and automated testing.**

---

## Features

- User registration
- JWT-based authentication
- Password hashing using BCrypt
- Role-based user model
- Wallet creation
- Wallet balance management
- Add money to wallet
- Wallet-to-wallet money transfer
- Transaction history
- Idempotent payment processing
- Pessimistic locking for concurrent wallet updates
- Double-spend protection
- Audit logging
- Request validation
- Global exception handling
- Swagger UI and OpenAPI documentation
- Docker containerization
- Automated unit and integration testing

---

## Tech Stack

| Technology | Usage |
|---|---|
| Java | Backend programming |
| Spring Boot | Backend framework |
| Spring Security | Authentication and authorization |
| JWT | Stateless authentication |
| Spring Data JPA | Database access |
| Hibernate | ORM |
| PostgreSQL | Relational database |
| Maven | Dependency management |
| Swagger / OpenAPI | API documentation and testing |
| Docker | Application containerization |
| Docker Compose | Multi-container application management, if configured |
| JUnit | Testing |
| Mockito | Unit testing |
| MockMvc | Controller and integration testing |
| Postman | API testing |
| Git & GitHub | Version control |

---

## Architecture

The application follows a layered architecture that separates API handling, business logic, data access, and persistence.

```text
Client / Swagger UI / Postman
              |
              v
      Spring Security
              |
              v
     JWT Authentication
              |
              v
      Controller Layer
              |
              v
       Service Layer
              |
              v
      Repository Layer
              |
              v
       PostgreSQL Database
```

Security is handled through JWT authentication before protected requests reach the controllers.

The layered architecture improves maintainability, testability, and separation of responsibilities.

---

## Core Modules

### Authentication

Users can register and log in.

Passwords are stored using BCrypt hashing rather than plain text.

After successful login, the system generates a JWT token that is used to access protected APIs.

### Wallet

Every registered user receives a wallet.

A wallet contains:

- Wallet number
- Balance
- Status
- Associated user

Wallet status can be:

```text
ACTIVE
BLOCKED
```

### Money Transfer

Users can transfer money to another registered user's wallet.

The transfer validates:

- Sender exists
- Receiver exists
- Sender and receiver are different
- Both wallets are active
- Sender has sufficient balance

The sender's balance is deducted and the receiver's balance is increased within the same database transaction.

---

## Idempotency

The transfer API supports idempotency using an idempotency key.

Example:

```text
Idempotency-Key: payment-001
```

If the same request is submitted again with the same idempotency key, the system is designed to prevent the payment from being processed twice.

This helps prevent duplicate payments caused by:

- Network retries
- Client retries
- Duplicate requests

The system stores idempotency information and its associated processing state.

Idempotency is an important feature in payment systems because clients may retry a request when a response is delayed or lost.

---

## Concurrency Control

Wallet transfers use pessimistic database locking.

The sender and receiver wallets are locked while the transfer is being processed.

This helps prevent concurrent requests from modifying the same wallet balance incorrectly.

The project uses:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

for wallet retrieval during transfers.

Consistent wallet-locking order also helps reduce the risk of deadlocks when multiple transfers involve the same wallets.

---

## Double-Spend Protection

The wallet balance is protected at multiple levels:

1. Application-level balance validation
2. Database transaction management
3. Pessimistic locking
4. Database constraint preventing negative balances

The wallet table can use a database-level constraint:

```sql
CHECK (balance >= 0)
```

This provides an additional safeguard against negative wallet balances when the constraint is present in the actual database schema.

---

## Transactions

Every successful transfer creates a transaction record containing:

- Transaction ID
- Sender wallet
- Receiver wallet
- Amount
- Status
- Creation timestamp

Transaction status currently includes:

```text
SUCCESS
FAILED
```

Users can retrieve their transaction history through the transaction API.

Database transactions help ensure that related wallet balance changes and transaction records remain consistent.

---

## Audit Logging

Important system actions are recorded in an audit log.

Tracked events include:

```text
USER_REGISTERED
USER_LOGIN
MONEY_ADDED
TRANSFER_SUCCESS
TRANSFER_FAILED
```

Audit records contain:

- User ID
- Event type
- Description
- Transaction ID where applicable
- Timestamp

Audit logging improves traceability and helps with debugging and investigating payment-related events.

Failure events should be recorded using transaction handling that preserves the audit record when the main payment transaction rolls back.

---

## Database Entities

The main entities are:

```text
User
 |
 | 1 : 1
 v
Wallet

Transaction

IdempotencyRecord

AuditLog
```

### User

Stores user account and authentication information.

### Wallet

Stores wallet number, balance, status, and user association.

### Transaction

Stores payment transaction details and status.

### IdempotencyRecord

Stores idempotency keys and their associated transaction state.

### AuditLog

Stores important system events for traceability.

---

## API Endpoints

### Authentication

#### Register User

```http
POST /api/users
```

Example request:

```json
{
  "name": "Smriti",
  "email": "smriti@example.com",
  "password": "password123"
}
```

#### Login

```http
POST /api/auth/login
```

Example request:

```json
{
  "email": "smriti@example.com",
  "password": "password123"
}
```

Returns a JWT token after successful authentication.

---

### Wallet

#### Get Wallet

```http
GET /api/wallet
```

Requires:

```text
Authorization: Bearer <JWT>
```

#### Add Money

```http
POST /api/wallet/add-money
```

Example:

```json
{
  "amount": 1000
}
```

#### Transfer Money

```http
POST /api/wallet/transfer
```

Headers:

```text
Authorization: Bearer <JWT>
Idempotency-Key: payment-001
```

Example:

```json
{
  "receiverEmail": "receiver@example.com",
  "amount": 300
}
```

---

### Transactions

#### Transaction History

```http
GET /api/transactions
```

Requires:

```text
Authorization: Bearer <JWT>
```

---

## Swagger API Documentation

The project uses Swagger and OpenAPI to document the REST APIs and provide an interactive interface for testing endpoints.

After starting the application, open Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

Swagger UI makes it easier to:

- Explore available API endpoints
- View request and response formats
- Understand API parameters
- Test endpoints interactively
- Test authenticated endpoints when JWT authorization is configured in Swagger

These URLs assume the application runs on port `8080` and the corresponding OpenAPI dependency is configured.

---

## Docker Containerization

Docker helps package the application and its runtime dependencies into a consistent environment.

If the project includes a configured `Dockerfile` and Docker Compose configuration, the application can be started using:

```bash
docker compose up --build
```

To run the containers in the background:

```bash
docker compose up -d --build
```

To stop the containers:

```bash
docker compose down
```

Docker support helps make application setup and deployment more consistent across development environments.

The Docker configuration should include the correct application settings, database connection, and required environment variables.

---

## Validation & Exception Handling

The application validates incoming requests using Jakarta Bean Validation.

Examples include:

- Required fields
- Valid email format
- Minimum password length
- Positive money amounts

The application also uses global exception handling to return structured error responses.

Examples:

- Invalid credentials
- Email already exists
- Insufficient balance
- Cannot transfer money to yourself
- Wallet not found
- Invalid request data

This improves API reliability and makes errors easier for clients to understand.

---

## Testing

The project includes unit tests, controller tests, and integration tests to verify the correctness of the application.

### Test Coverage

The test suite covers:

- User registration
- Authentication and JWT login
- Wallet operations
- Add money
- Successful money transfer
- Insufficient balance handling
- Self-transfer validation
- Idempotency
- Transaction history
- Request validation
- Controller behavior
- Database integration

### Running Tests

Execute the complete test suite using Maven:

```bash
./mvnw clean test
```

Review the latest Maven output for the actual number of passing tests and the final build status.

### Testing Approach

The project uses multiple levels of testing:

```text
Unit Tests
    |
    v
Controller Tests
    |
    v
Integration Tests
    |
    v
Database Verification
```

Integration tests use MockMvc to test HTTP request flows through the Spring Boot application.

---

## Running the Project Locally

### Prerequisites

Make sure the following are installed:

- Java version supported by the project
- PostgreSQL
- Maven or Maven Wrapper
- Docker, if using the Docker setup

### 1. Clone the Repository

```bash
git clone https://github.com/SmritiSingh18/Payment-Wallet-System.git
cd Payment-Wallet-System
```

### 2. Configure PostgreSQL

Create a PostgreSQL database named:

```text
payment_wallet
```

Update the database configuration in:

```text
src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/payment_wallet
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}
```

Configure the required credentials and JWT settings using environment variables or a local configuration file. Do not commit passwords or signing secrets to GitHub.

### 3. Run the Application

Using the Maven Wrapper:

```bash
./mvnw spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

### 4. Access Swagger UI

Once the application has started, open:

```text
http://localhost:8080/swagger-ui/index.html
```

### 5. Run with Docker

If Docker Compose is configured:

```bash
docker compose up --build
```

---

## API Testing

The APIs can be tested using Postman and Swagger UI.

### Main Payment Flow

```text
Registration
     |
     v
Login
     |
     v
JWT Authentication
     |
     v
Access Wallet
     |
     v
Add Money
     |
     v
Transfer Money
     |
     v
Transaction History
```

### Additional Scenarios

Duplicate payment request:

```text
Duplicate Payment Request
          |
          v
Idempotency Protection
```

Insufficient balance:

```text
Insufficient Balance
          |
          v
Transfer Rejected
```

Concurrent balance updates:

```text
Concurrent Balance Updates
          |
          v
Pessimistic Locking
```

Invalid or unauthenticated request:

```text
Invalid / Unauthenticated Request
          |
          v
Access Rejected
```

---

## Project Structure

```text
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── payment/
│   │           └── wallet_system/
│   │               ├── config/
│   │               ├── controller/
│   │               ├── dto/
│   │               ├── entity/
│   │               ├── exception/
│   │               ├── repository/
│   │               ├── security/
│   │               └── service/
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/
            └── payment/
                └── wallet_system/
```

The exact package structure may vary depending on the current implementation.

---

## Future Improvements

Potential future improvements include:

- Redis-based distributed locking and idempotency where required
- Refresh-token based authentication
- Improved API response standards
- Monitoring and observability
- Cloud deployment
- CI/CD automation
- Enhanced concurrent payment testing

---

## Author

**Smriti Singh**

B.E. Information Technology

GitHub:

https://github.com/SmritiSingh18/Payment-Wallet-System

Repository:

https://github.com/SmritiSingh18/Payment-Wallet-System

---
