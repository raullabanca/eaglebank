# 🦅 EagleBank – Banking API

A secure, extensible RESTful API for managing user bank accounts and transactions, built with *
*Spring Boot 3** and **Java 21**. This application supports features like account creation,
deposits, withdrawals, and transaction history, all authenticated via JWT.

---

## 🧰 Technologies Used

- **Java 21**
- **Spring Boot 3.x**
    - Spring Web
    - Spring Data JPA
    - Spring Security
    - Spring Validation
- **H2 In-Memory Database**
- **Jakarta Annotations**
- **JUnit 5 & MockMvc** – Integration & Unit Testing
- **Docker**
- **OpenAPI 3 / Swagger UI**
- **Maven**

---

## 🚀 Getting Started

### ✅ Requirements

- Java 21+
- Maven 3.9+
- Docker (optional, for containerization)

---

## 🔧 Build the Application

Build the executable JAR with the following:

```bash
./mvnw clean package
```

This will generate the executable JAR file in the target/ directory.

## 🧪 Run Locally

Run the app using:

```bash
java -jar target/eagle-0.0.1-SNAPSHOT.jar
```

or

```bash
./mvnw clean spring-boot:run
```

Once running, the API is available at: http://localhost:8080

## 🐳 Run with Docker

Step 1: Build the Docker Image
Make sure the Dockerfile is located at src/docker/Dockerfile. Then run:

```bash
docker build -f src/docker/Dockerfile -t eaglebank .
```

Step 2: Run the Container

```bash
docker run --network=host -p 8080:8080 eaglebank
```

## 📖 API Documentation

Once running, access the Swagger UI: http://localhost:8080/swagger-ui.html

This provides an interactive UI for testing endpoints and viewing schema definitions (/v1/accounts,
/v1/accounts/{accountNumber}/transactions, etc.).

## 🧪 Testing

To execute all integration and unit tests:

```bash
./mvnw test
```

Tests cover:

- Account creation, listing, and updating

- Transaction creation and listing

- Error scenarios like unauthorized access and invalid input

## 📂 Project Structure

```text
eaglebank/
├── src/
│   ├── main/
│   │   ├── java/com/eaglebank/
│   │   │   ├── config/               
│   │   │   ├── controller/           
│   │   │   ├── dto/                  
│   │   │   ├── exception/            
│   │   │   ├── mapper/               
│   │   │   ├── model/                
│   │   │   ├── repository/           
│   │   │   ├── security/             
│   │   │   ├── service/              
│   │   │   ├── utils/              
│   │   │   └── EaglebankApplication.java
│   │   └── resources/
│   │       ├── application.yml       
│   │       └── logback.xml              
│   └── docker/
│       └── Dockerfile                
│
├── test/
│   └── java/com/eaglebank/
│       ├── TransactionITests.java    
│       └── (other test classes)      
│
├── target/                           
│
├── pom.xml                           
├── .gitignore                                             
└── README.md   

```

## 🛡️ Authentication

Authentication is handled via JWT. Only authenticated users can create or access bank accounts and
transactions.

Tokens must be passed in the Authorization: Bearer <token> header.

## 🔒 Error Handling

Standard error responses are provided for:

400 Bad Request

401 Unauthorized

403 Forbidden

404 Not Found

422 Unprocessable Entity

500 Internal Server Error

Custom exceptions are mapped to these status codes via @ControllerAdvice.

## 📈 Supported Features

✅ User authentication (JWT-based)

✅ Create/update/view bank accounts

✅ Deposit and withdraw funds

✅ List and fetch transactions by ID

✅ Ownership and permission checks

✅ In-memory DB for quick local testing

## 📝 Notes

This application is built with in-memory H2 DB, so data resets on restart. Use PostgreSQL or MySQL
for persistence in production.

Transactions are ACID-compliant via @Transactional.