# ☕ Client Contract API

A RESTful API built with **Spring Boot (Java 21)** and **PostgreSQL**, designed to manage **clients** (persons or companies) and their associated **contracts**.

This project was developed as part of the **technical assessment** for the **Java / Spring Boot Developer** position at **Vaudoise Assurances – API Factory**.

---

## ⚙️ Run Locally

### 1️⃣ Clone the repository
```bash
git clone https://github.com/<your-user>/client-contract-api.git
cd client-contract-api
```

### 2️⃣ Start PostgreSQL (Docker)
```bash
make db-up
```
- DB: `client_contract_db`
- URL: `jdbc:postgresql://localhost:5433/client_contract_db`
- Username: `postgres`
- Password: `postgres`

### 3️⃣ Run the Spring Boot app
```bash
make run-dev
```
→ The API will be available at: [http://localhost:8080/api](http://localhost:8080/api)

---

## 🧭 API Documentation

Swagger UI and OpenAPI 3 specification are automatically generated at runtime.

| Resource | URL |
|-----------|-----|
| Swagger UI | [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html) |
| OpenAPI JSON | [http://localhost:8080/api/v3/api-docs](http://localhost:8080/api/v3/api-docs) |
| OpenAPI YAML | [http://localhost:8080/api/v3/api-docs.yaml](http://localhost:8080/api/v3/api-docs.yaml) |

---

## 🧩 API Overview

| Feature | Endpoint | Method |
|----------|-----------|--------|
| Create person client | `/v1/clients/person` | POST |
| Create company client | `/v1/clients/company` | POST |
| Get client by ID | `/v1/clients/{id}` | GET |
| Update client | `/v1/clients/{id}` | PUT |
| Delete client | `/v1/clients/{id}` | DELETE |
| Create contract | `/v1/contracts` | POST |
| Update contract (cost only) | `/v1/contracts/{id}` | PUT |
| List active contracts | `/v1/clients/{id}/contracts/active` | GET |
| Sum of active contracts | `/v1/clients/{id}/contracts/active/sum` | GET |

---

## 🧠 Architecture & Design

This API follows a **layered architecture (Controller → Service → Repository)** using **Spring Boot 3.5**, **Spring Data JPA**, and **PostgreSQL**.  
Entities are persisted through **JPA/Hibernate** and exposed as DTOs via **MapStruct**, ensuring a clean separation between data and presentation layers.  
Validation is handled using **Jakarta Validation** annotations (`@Email`, `@Pattern`, `@Positive`, etc.).  
Soft-deletion is implemented at the service layer: when a client is deleted, its active contracts are automatically closed by setting their end date to the current date.  
Error handling is centralized with a `GlobalExceptionHandler`, providing consistent JSON responses.  
Endpoints use clear REST semantics (`201 Created`, `404 Not Found`, `409 Conflict`) and demonstrate scalability via pagination, filtering, and aggregation patterns.

---

## ✅ Proof

All core use cases are covered by **JUnit 5** and **MockMvc** tests in `src/test/java`.  
To verify locally:
```bash
./mvnw test
```

---

## 👤 Author

**Danny Albuquerque Ferreira**  
