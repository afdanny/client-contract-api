# ☕ Client Contract API

A RESTful API built with **Spring Boot (Java 21)** and **PostgreSQL**, designed to manage **clients** (persons or companies) and their associated **contracts**.

This project was developed as part of the **technical assessment** for the **Java / Spring Boot Developer** position at **Vaudoise Assurances – API Factory**.

---

## ⚙️ Setup & Run

### 🧰 1️⃣ Prerequisites

Before running the app, ensure you have:

| Tool | Minimum Version | Installation |
|------|------------------|---------------|
| **Java** | 21+ | [https://learn.microsoft.com/en-us/java/openjdk/download](https://learn.microsoft.com/en-us/java/openjdk/download) |
| **Maven** | *(optional)* – a wrapper (`mvnw`) is already provided | |
| **Docker** | 20+ | [https://docs.docker.com/get-docker](https://docs.docker.com/get-docker) |
| **Docker Compose** | v2+ *(usually included with Docker Desktop)* | |

> 🧩 If you don’t have Docker installed, download **Docker Desktop** from the link above (available for Windows, macOS, and Linux).  
> Alternatively, you can point the app to an external PostgreSQL instance — see `src/main/resources/application-dev.yml` for configuration details.

---

### 🐘 2️⃣ Start the PostgreSQL environment

**Option A — Recommended (with `make`):**
```bash
make db-up
```

**Option B — Manual (if you don’t have `make`):**
```bash
docker compose up -d
```

This will start:
- **PostgreSQL** on `localhost:5433`
- **Adminer UI** on [http://localhost:8081](http://localhost:8081)

Default credentials:
```
Host: localhost
Port: 5433
Database: client_contract_db
User: postgres
Password: postgres
```

---

### 🚀 3️⃣ Run the Spring Boot app

**Option A — With `make`:**
```bash
make run-dev
```

**Option B — Manual:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
→ The API will be available at: [http://localhost:8080/api](http://localhost:8080/api)

---

### 📘 Explore the API

Once the app is running:

| Tool | URL |
|------|-----|
| **Swagger UI** | [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html) |
| **OpenAPI JSON** | [http://localhost:8080/api/v3/api-docs](http://localhost:8080/api/v3/api-docs) |

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

This API follows a layered architecture (Controller → Service → Repository) built with Spring Boot 3.5, Spring Data JPA, and PostgreSQL.

Entities are managed via Hibernate ORM and mapped to DTOs using MapStruct.

Validation is enforced through Jakarta Bean Validation annotations (@Email, @Pattern, @Positive, etc.), and error handling is centralized in a GlobalExceptionHandler, providing consistent JSON responses.

Soft deletion is implemented at the service layer, allowing entities to be logically deleted while preserving their historical data.

Endpoints follow REST conventions (201 Created, 404 Not Found, 409 Conflict, etc.) and are documented via OpenAPI 3 / Swagger UI for easy exploration and testing.

Liquibase is integrated for future production deployment and database migration management, ensuring reproducible and version-controlled schema evolution.

---

## ✅ Proof

All core use cases are covered by **JUnit 5** and **MockMvc** tests in `src/test/java`.  
To verify locally:
```bash
./mvnw test
```

---

## 🧰 Makefile Commands

| Command | Description |
|----------|-------------|
| `make db-up` | Start PostgreSQL (5433) and Adminer (8081) |
| `make db-down` | Stop containers but keep the data volume |
| `make db-clean` | Stop and remove containers + volumes (DB reset) |
| `make run-dev` | Run Spring Boot with the `dev` profile |
| `make logs` | Follow Docker logs |
| `make status` | Display running containers |
| `make psql` | Open a PostgreSQL shell inside the container |
| `make help` | Display available Make targets |

---

## 👤 Author

**Danny Albuquerque Ferreira**  
