# Nexus | Workflow Execution Platform

![Java 17](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-brightgreen?style=flat-square&logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-6DB33F?style=flat-square&logo=springsecurity)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=flat-square&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=flat-square&logo=apachemaven)

**Nexus** is a backend platform for workflow execution and task orchestration. Built as a modular Spring Boot service with explicit domain boundaries, it demonstrates transactional consistency, idempotency enforcement, and stateless JWT authentication in a single-database architecture.

---

## Core Design

**Modular Monolith + Single-Database Consistency**

- Single deployable unit with clear domain module separation
- All writes through PostgreSQL—transactional integrity maintained within one database connection
- Layered architecture: Controller → Service → Repository → Database
- Composite database constraints enforce idempotency at application and storage layers
- Module boundaries support clean extraction if service independence becomes necessary

Maintains transactional consistency and operational simplicity while preserving clear module boundaries.

---

## Architecture

### Module Organization

| Module | Responsibility |
| --- | --- |
| `auth` | User registration, login, password hashing (BCrypt), JWT issuance |
| `task` | Task CRUD, ownership validation, streak computation, leaderboard aggregation |
| `execution` | Execution logging, per-task history, idempotency validation |
| `common` | Security configuration, OpenAPI setup, exception handling, utilities |
| `system` | Health and availability endpoints |

### Workflow Execution Model

**Ownership and idempotency are enforced in layers:**

- **Task ownership:** Each task belongs to a single authenticated user
- **Execution logging:** Events recorded per task per calendar date
- **Idempotency enforcement:**
  - Service layer checks for existing execution record before accepting new entry
  - Database composite unique constraint on `(task_id, date)` prevents duplicates if app-layer checks are bypassed
  - Ensures correctness under concurrent requests within a single transaction
- **Authorization:** Service layer validates ownership on all workflow access paths
- **Consistency metrics:** Streaks and leaderboard rankings computed from persisted execution history

### Request Flow

```
Controller → Service → Repository → PostgreSQL
```

Clear responsibility separation: HTTP contracts → business logic → persistence abstraction → storage.

---

## Tech Stack & APIs

| Category | Technology |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security, JWT (stateless bearer tokens) |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL 15 |
| API Docs | Springdoc OpenAPI / Swagger UI |
| Build | Maven |
| Containers | Docker, Docker Compose |
| Testing | JUnit 5, Mockito, MockMvc |
| CI/CD | GitHub Actions |

### Core Endpoints

| Area | Endpoints |
| --- | --- |
| **Auth** | `POST /api/v1/auth/register`, `POST /api/v1/auth/login` |
| **Tasks** | `POST /api/v1/tasks`, `GET /api/v1/tasks` |
| **Execution** | `POST /api/v1/tasks/{taskId}/execution`, `GET /api/v1/tasks/{taskId}/execution` |
| **Metrics** | `GET /api/v1/tasks/{taskId}/streak`, `GET /api/v1/tasks/leaderboard` |
| **System** | `GET /api/system/health` |

**API Documentation:**
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Responses follow a consistent JSON envelope. Errors include structured details: status code, type, message, timestamp, and path.

---

## Security

### Authentication & Authorization

- **JWT validation:** Stateless bearer token authentication on all protected endpoints
- **Spring Security context:** Token hydrated on each request; unauthenticated requests receive 401
- **Password security:** BCrypt hashing via Spring Security's `BCryptPasswordEncoder`
- **Authorization model:** Resource ownership validated at service layer—tasks belong to users; only the owner can read or modify
- **Implementation:** Authorization logic is implemented at the service layer for explicit ownership validation and maintainability

### Configuration

- `SessionCreationPolicy.STATELESS` ensures no session storage
- OpenAPI bearer metadata enables direct Swagger testing of secured endpoints

---

## Database Design

### Single-Database Consistency

- **Task and execution state** colocated in PostgreSQL—all writes synchronous and within a single transaction
- **Composite unique constraints** on `(task_id, date)` enforce idempotency at the storage layer
- **Service-layer validation** performs checks before persistence; database serves as the final consistency guard
- **Atomic updates:** All changes complete within one transaction

### Schema Management

**Development:** `spring.jpa.hibernate.ddl-auto=update` for convenience.

**Deployment:** Replace with **Flyway** or **Liquibase** for:
- Reproducible, auditable schema migrations
- Safe rollback and team collaboration
- Schema change history

---

## Getting Started

### Local Development

**Requirements:** Java 17, Maven (included), PostgreSQL 15+, Docker (optional)

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run

# Test
./mvnw test
```

**Windows PowerShell:**
```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test
```

### Docker Compose

```bash
docker compose up --build
```

**Stack includes:**
- Multi-stage `Dockerfile` with minimal runtime image
- PostgreSQL 15 with health checks
- Non-root container user
- Environment-driven configuration

**Access:**
- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- PostgreSQL: `localhost:5433`

```bash
docker compose down  # Stop stack
```

### Environment Configuration

| Variable | Purpose | Default |
| --- | --- | --- |
| `PORT` | HTTP server port | `8080` |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/execution_os` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `execution_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `secure123` |
| `JWT_SECRET` | JWT signing secret | (required in non-local environments) |

**Operational requirements:**
- Externalize secrets through environment variables
- Use strong JWT secrets (≥32 characters entropy)
- Disable verbose SQL logging in non-local environments
- Replace `ddl-auto=update` with explicit migration tooling

---

## CI/CD

GitHub Actions (`.github/workflows/`) automates:
- Maven build and unit tests on push to `main`
- Docker image builds with multi-stage caching
- Container image publication

---

## Project Structure

```
src/
  main/
    java/com/executionos/
      auth/            # User registration, login, JWT
      task/            # Task management, metrics
      execution/       # Execution logging, idempotency
      common/          # Security, OpenAPI, utilities
      system/          # Health checks
    resources/
      application.yaml
  test/
    java/com/executionos/
      auth/
      task/
      execution/
Dockerfile
docker-compose.yml
pom.xml
```

---

## Features

- ✅ Stateless JWT authentication with bearer token validation
- ✅ User-scoped resource access with service-layer ownership validation
- ✅ Dual-layer idempotency enforcement (application + database constraints)
- ✅ Transactional execution logging with per-day granularity
- ✅ Streak and leaderboard aggregation from persisted history
- ✅ OpenAPI/Swagger UI for interactive API exploration
- ✅ Docker Compose for local and runtime environments
- ✅ Spring Boot Actuator health monitoring
- ✅ GitHub Actions CI integration

---

## License

This project is licensed under the [MIT License](LICENSE).
