# Payment & Wallet System

A secure backend payment and wallet system built using Java and Spring Boot.

The project simulates a wallet-based payment system where users can register, authenticate using JWT, add money to their wallets, transfer money between wallets, and view transaction history.

The system focuses on important backend concepts such as **idempotency, concurrency control, double-spend prevention, audit logging, validation, exception handling, and automated testing.**

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
| JUnit | Testing |
| Mockito | Unit testing |
| MockMvc | Controller and integration testing |
| Postman | API testing |
| Git & GitHub | Version control |

---

## Architecture

The application follows a layered architecture that separates API handling, business logic, data access, and persistence.

```text
Client
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

Security is handled through a JWT authentication filter before protected requests reach the controllers.

```text
Client
   |
   | JWT
   v
Spring Security
   |
   v
JWT Authentication Filter
   |
   v
Controller
   |
   v
Service
   |
   v
Repository
   |
   v
PostgreSQL
```

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

The sender's balance is deducted and the receiver's balance is increased within the same transaction.

---

## Idempotency

The transfer API supports idempotency using an idempotency key.

Example:

```text
Idempotency-Key: payment-001
```

If the same request is submitted again with the same idempotency key, the system does not process the payment again.

This prevents duplicate payments caused by:

- Network retries
- Client retries
- Duplicate requests

The system stores the idempotency key along with the transaction and processing status.

---

## Concurrency Control

Wallet transfers use pessimistic database locking.

The sender and receiver wallets are locked while the transfer is being processed.

This helps prevent two concurrent requests from modifying the same wallet balance incorrectly.

The project uses:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

for wallet retrieval during transfers.

---

## Double-Spend Protection

The wallet balance is protected at multiple levels:

1. Application-level balance validation
2. Database transaction management
3. Pessimistic locking
4. Database constraint preventing negative balances

The wallet table also contains a database-level constraint:

```sql
CHECK (balance >= 0)
```

This provides an additional safeguard against negative wallet balances.

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

---

## Audit Logging

Important system actions are recorded in an audit log.

Currently tracked events include:

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

Audit logging for failed transfers uses a separate transaction so that important failure events are preserved even when the main payment transaction rolls back.

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

Stores successful and failed payment transactions.

### IdempotencyRecord

Stores idempotency keys and their associated transaction state.

### AuditLog

Stores important system events for traceability.

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

Returns a JWT token.

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

### Automated Test Results

The complete test suite was executed using Maven:

```bash
./mvnw clean test
```

Result:

```text
Tests run: 39
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

This confirms that all **39 automated tests passed successfully**.

### Testing Approach

The project uses multiple levels of testing:

```text
Unit Tests
    ↓
Controller Tests
    ↓
Integration Tests
    ↓
Database Verification
```

Integration tests use **MockMvc** to test complete request flows through the Spring Boot application and verify important state changes in PostgreSQL.

---


## Running the Project Locally

### Prerequisites

Make sure the following are installed:

- Java
- PostgreSQL
- Maven or Maven Wrapper

### 1. Clone the Repository

```bash
git clone https://github.com/SmritiSingh18/Payment-Wallet-System.git
cd Payment-Wallet-System
```

### 2. Configure PostgreSQL

Create a PostgreSQL database:

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
spring.datasource.username=<your-username>
spring.datasource.password=<your-password>
```

> Do not commit real passwords or secret keys to GitHub.

### 3. Run the Application

Using the Maven Wrapper:

```bash
./mvnw spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

---

## Running Tests

Run the complete test suite:

```bash
./mvnw clean test
```

Expected result:

```text
Tests run: 39
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

---

## API Testing

The APIs were tested using Postman.

### Main Payment Flow

```text
Registration
     ↓
Login
     ↓
JWT Authentication
     ↓
Access Wallet
     ↓
Add Money
     ↓
Transfer Money
     ↓
Transaction History
```

### Additional Scenarios

```text
Duplicate Payment Request
        ↓
Idempotency Protection
```

```text
Insufficient Balance
        ↓
Transfer Rejected
```

```text
Concurrent Balance Updates
        ↓
Pessimistic Locking
```

```text
Invalid / Unauthenticated Request
        ↓
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
│   │
│   └── resources/
│       └── application.properties
│
└── test/
    └── java/
        └── com/
            └── payment/
                └── wallet_system/
```

---

## Future Improvements

Potential future improvements include:

- Swagger/OpenAPI documentation
- Docker containerization
- Redis-based distributed locking
- Deployment to a cloud platform
- Refresh-token based authentication
- Improved API response standards
- Monitoring and observability

---

## Author

**Smriti Singh**

B.E. Information Technology

GitHub:

https://github.com/SmritiSingh18/Payment-Wallet-System

---