# 🏗️ Client Contract API

> **Spring Boot + PostgreSQL + Liquibase + Docker Compose**

This project provides a RESTful API to manage **clients** (either persons or companies) and their associated **contracts**.  
It has been implemented as part of the **technical assessment for the Java / Spring Boot Developer position** within the **API Factory team at Vaudoise Assurances**.

---

## 🚀 Tech Stack

| Area | Technology |
|-------|-------------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.6 |
| Persistence | JPA / Hibernate |
| Database | PostgreSQL 16 (Dockerized) |
| Schema Management | Liquibase |
| Build Tool | Maven Wrapper (`./mvnw`) |
| Testing | JUnit 5 / Spring Boot Test |
| Containerization | Docker Compose |
| Configuration | YAML profiles (`dev` / `prod`) |

---

## ⚙️ Setup & Run

### 1️⃣ Clone the repository
```bash
git clone https://github.com/<your-user>/client-contract-api.git
cd client-contract-api
```

### 2️⃣ Start the database environment
```bash
make db-up
```

Services:
- **PostgreSQL** → `localhost:5433`
- **Adminer UI** → [http://localhost:8081](http://localhost:8081)
  - Server: `postgres`
  - Username: `postgres`
  - Password: `postgres`
  - Database: `client_contract_db`

### 3️⃣ Launch the Spring Boot app (dev profile)
```bash
make run-dev
```

> This command starts the application with the `dev` profile and automatically applies Liquibase migrations.

---

## 🧱 Project Structure

```
client-contract-api/
├── docker-compose.yml                 # Dockerized PostgreSQL + Adminer
├── Makefile                           # Developer productivity commands
├── pom.xml                            # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/ch/afdanny/technicalexercise/clientcontractapi/
│   │   │   └── ClientContractApiApplication.java
│   │   └── resources/
│   │       ├── application.yml        # Global config
│   │       ├── application-dev.yml    # Development profile
│   │       ├── application-prod.yml   # Production profile
│   │       └── db/changelog/          # Liquibase changelogs
│   │           ├── changelog-master.xml
│   │           └── 000-init-schema.xml
│   └── test/
│       └── java/...                   # Integration tests
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

## 🧩 Database Migrations (Liquibase)

Liquibase is used to version and manage all schema changes.

**Files:**
- `changelog-master.xml` → root changelog entry
- `000-init-schema.xml` → simple verification table (`liquibase_test`)

At application startup:
1. Liquibase connects to the database defined by the active Spring profile.
2. Executes pending changelogs in order.
3. Tracks applied changes in `databasechangelog` and `databasechangeloglock`.

---

## 🧪 Verification

### Check Liquibase tables
```bash
make psql
# then inside psql:
\dt
```
Expected output:
```
databasechangelog
databasechangeloglock
liquibase_test
```

### Check PostgreSQL health
```bash
docker exec -it client-contract-postgres pg_isready -U postgres -d client_contract_db
```

---

## 📘 License

This project was developed **for demonstration and evaluation purposes**  
within the technical assessment for **Vaudoise Assurances – API Factory**.

---

## 👨‍💻 Author

**Danny Albuquerque Ferreira**
