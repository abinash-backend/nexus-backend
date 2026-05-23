# Nexus | Workflow Execution Platform

![Java 17](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-brightgreen?style=flat-square&logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-6DB33F?style=flat-square&logo=springsecurity)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=flat-square&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=flat-square&logo=apachemaven)
![OpenAPI](https://img.shields.io/badge/API-OpenAPI-85EA2D?style=flat-square&logo=swagger)
![Architecture](https://img.shields.io/badge/Architecture-Modular%20Monolith-333333?style=flat-square)

**Nexus** is a backend platform for workflow execution, task orchestration, and execution lifecycle tracking. Built as a modular Spring Boot service with explicit domain boundaries, stateless REST APIs, and production-grade security and operational patterns.

The system emphasizes reliable workflow state evolution, ownership enforcement, and data integrity without premature distributed complexity. Architected as a monolith today with clear module separation designed to support future service extraction if scaling demands justify it.

## Overview

Nexus models workflow execution as a controlled, auditable lifecycle:

1. An authenticated user registers or signs in and receives a JWT.
2. The user creates tasks that become workflow units owned by that identity.
3. Execution events are recorded against those tasks on a per-day basis.
4. The platform enforces ownership and prevents duplicate same-day execution records through application and database layer validation.
5. Read models expose task state, execution history, and consistency metrics.

This keeps the platform centered on operational accountability rather than simple CRUD operations. The main design goal is reliable workflow state evolution inside a clearly modeled, layered architecture.

## Architecture

### Architectural Direction

- **Modular monolith:** Single deployable unit with explicit domain module separation
- **Domain-driven module boundaries:** Each business capability owns its controllers, services, repositories, and entities
- **Layered application structure:** Controller → Service → Repository → PostgreSQL
- **Single-database consistency model:** Transactional integrity maintained via relational constraints
- **REST API boundary:** Designed to enable future service extraction with minimal refactoring

### Why Modular Monolith

Nexus is architected as one deployable application while separating business capabilities into domain modules. This choice optimizes for:

- **Operational simplicity:** Single deployment process, straightforward local development, no inter-service coordination overhead
- **Transactional consistency:** All writes go through one database connection, preventing distributed coordination complexity
- **Developer velocity:** No service discovery, timeout handling, or retry logic to debug during feature development
- **Clear extraction path:** Module boundaries and REST API layers are designed so that if scaling pressure justifies service independence, extraction requires only configuration changes, not core refactoring

This is not a premature microservices claim. The architecture is intentionally scaled to current constraints while preserving the option to distribute if requirements change.

### Module Boundaries

| Module | Responsibility |
| --- | --- |
| `auth` | User registration, login, password hashing (BCrypt), and JWT issuance |
| `task` | Task creation, retrieval, filtering, streak computation, and leaderboard assembly |
| `execution` | Execution logging, per-task execution history, and execution idempotency enforcement |
| `common` | Shared security configuration, OpenAPI setup, exception handling, and cross-cutting utilities |
| `system` | Health and service availability endpoints |

### Layered Flow

```
Controller → Service → Repository → PostgreSQL
```

Each module follows this layered path:

- **Controllers** define HTTP contracts and request mapping
- **Services** orchestrate workflow rules and business logic
- **Repositories** isolate persistence access through Spring Data JPA
- **Entities and DTOs** keep domain and transport concerns separated

## Workflow Execution Model

The workflow subsystem is built around task ownership and execution traceability:

- A task belongs to a single user (owner)
- Execution logs are recorded against a task and a specific calendar date
- A composite database uniqueness constraint (`task_id`, `date`) prevents duplicate execution entries for the same task on the same day
- Service-layer ownership checks reject cross-user access to workflow state
- Consistency metrics (streaks, leaderboard rankings) are derived from persisted execution history rather than in-memory transient state

**Idempotency Strategy:**

Duplicate execution prevention is enforced at both application and database layers:

1. **Application layer:** Service checks for existing execution record before accepting new log
2. **Database layer:** Composite unique constraint on `(task_id, date)` prevents duplicate inserts if application layer checks are bypassed

This dual-layer approach ensures correctness under concurrent request scenarios without distributed coordination.

## Feature Highlights

- **Stateless JWT authentication** for protected APIs with bearer token validation
- **User-scoped task creation and retrieval** with ownership enforcement at service layer
- **Execution lifecycle logging** per task with per-day granularity
- **Ownership enforcement** on all workflow access paths, preventing unauthorized cross-user access
- **Duplicate execution prevention** through application-layer validation and database constraints
- **Streak and consistency calculations** derived from persisted execution history
- **Leaderboard-style aggregation** for user consistency scoring and ranking
- **OpenAPI documentation** with interactive Swagger UI for API exploration and testing
- **Containerized runtime** with Docker and Docker Compose for reproducible local and deployment environments
- **Actuator-backed health monitoring** for operational visibility
- **GitHub Actions CI** for automated build and test verification on code push

## Tech Stack

| Category | Technology |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security, JWT (stateless bearer tokens) |
| Validation | Jakarta Bean Validation |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL 15 |
| API Documentation | Springdoc OpenAPI / Swagger UI |
| Build | Maven Wrapper |
| Containers | Docker, Docker Compose |
| Testing | JUnit 5, Mockito, MockMvc |
| CI/CD | GitHub Actions |

## Project Structure

```text
src/
  main/
    java/com/executionos/
      auth/
        controller/
        dto/
        entity/
        mapper/
        repository/
      common/
        config/
        exception/
        response/
        security/
        util/
      execution/
        controller/
        dto/
        entity/
        mapper/
        repository/
        service/
      system/
        controller/
      task/
        controller/
        dto/
        entity/
        mapper/
        repository/
        service/
      ExecutionOsBackendApplication.java
    resources/
      application.yaml
  test/
    java/com/executionos/
      auth/controller/
      execution/controller/
      execution/service/
      task/controller/
      task/service/
Dockerfile
docker-compose.yml
pom.xml
README.md
```

## Security Implementation

Security is implemented as a stateless request pipeline with explicit authorization checks:

- **Spring Security configuration** set to stateless mode (`SessionCreationPolicy.STATELESS`)
- **JWT authentication filter** validates bearer tokens on each request and hydrates the Spring Security context
- **Protected endpoints** require an authenticated principal; unauthenticated requests receive 401 responses
- **Passwords stored with BCrypt hashing** using Spring Security's `BCryptPasswordEncoder`
- **Service-layer authorization checks** enforce resource ownership using the authenticated user identifier
- **OpenAPI configuration** includes bearer authentication metadata for interactive Swagger testing

### Authorization Model

Resource authorization is enforced at the service layer, not just at the endpoint level:

- **Authentication:** Validated via JWT bearer token
- **Authorization:** Ownership scoped—a task belongs to a user; only that user can read or modify it
- **No framework magic:** Authorization checks are explicit in service code, not hidden in annotations

This approach is simple, auditable, and scales to more complex RBAC if needed later.

## Database and Transactional Consistency

Nexus uses PostgreSQL as the system of record and maintains write consistency inside a single relational boundary:

- **Task and execution state** live in the same database, keeping workflow updates synchronous and predictable
- **Composite unique constraints** on `(task_id, date)` prevent duplicate execution records at the database level
- **Service-layer validation** performs application-level checks before persistence, with the database serving as the final consistency guard
- **Single-node transactional integrity:** All writes complete within a single transaction; no distributed coordination required

### Schema Management

The current development configuration uses `spring.jpa.hibernate.ddl-auto=update` for convenience.

**For production deployments:**

Replace `ddl-auto=update` with explicit schema migrations using **Flyway** or **Liquibase**. This ensures:
- Reproducible schema changes across environments
- Safe rollback capabilities
- Audit trail of schema evolution
- Team collaboration on database changes

See [Scalability Roadmap](#scalability-roadmap) for migration tooling integration.

## API Surface

| Area | Endpoints |
| --- | --- |
| **Auth** | `POST /api/v1/auth/register`, `POST /api/v1/auth/login` |
| **Tasks** | `POST /api/v1/tasks`, `GET /api/v1/tasks` |
| **Execution** | `POST /api/v1/tasks/{taskId}/execution`, `GET /api/v1/tasks/{taskId}/execution` |
| **Metrics** | `GET /api/v1/tasks/{taskId}/streak`, `GET /api/v1/tasks/leaderboard` |
| **System** | `GET /api/system/health` |

All responses follow a consistent JSON envelope. Errors include structured details: status code, error type, message, timestamp, and request path.

## API Documentation

Swagger UI and OpenAPI are enabled through Springdoc:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`
- **Health endpoint:** `http://localhost:8080/api/system/health`

The OpenAPI configuration includes bearer authentication metadata, allowing secured endpoints to be exercised directly from the Swagger interface.

## Docker Setup

The repository includes:

- A multi-stage `Dockerfile` with minimal runtime image size
- A `docker-compose.yml` stack for backend and PostgreSQL
- PostgreSQL health checks using `pg_isready`
- Non-root runtime container user for security
- Environment-driven datasource configuration for container deployment

### Start the Stack

```bash
docker compose up --build
```

### Stop the Stack

```bash
docker compose down
```

### Container Access

- **API:** `http://localhost:8080`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **PostgreSQL host port:** `5433`

## Local Development

### Prerequisites

- Java 17
- Maven wrapper support (included)
- PostgreSQL 15+ (or use Docker Compose to run infrastructure only)
- Docker Desktop or Docker Engine for containerized runs

### Build

```bash
./mvnw clean package
```

**Windows PowerShell:**

```powershell
.\mvnw.cmd clean package
```

### Run Locally

```bash
./mvnw spring-boot:run
```

**Windows PowerShell:**

```powershell
.\mvnw.cmd spring-boot:run
```

### Run Tests

```bash
./mvnw test
```

**Windows PowerShell:**

```powershell
.\mvnw.cmd test
```

## Environment Configuration

The application reads configuration from environment variables with defaults in `src/main/resources/application.yaml`:

| Variable | Purpose | Default |
| --- | --- | --- |
| `PORT` | HTTP server port | `8080` |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/execution_os` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `execution_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `secure123` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate schema strategy | `update` |
| `JWT_SECRET` | JWT signing secret | (required in production) |

### For Non-Local Environments

- Move all secrets into environment-managed configuration (never commit credentials)
- Externalize JWT signing secrets with sufficient entropy (minimum 32 characters)
- Disable verbose SQL logging
- Replace `ddl-auto=update` with explicit Flyway or Liquibase migrations
- Enable HTTPS and secure cookie flags

## CI/CD

The repository includes GitHub Actions workflows under `.github/workflows/`:

- **Maven build and verification** on pushes to `main`
- **Docker image build** with multi-stage caching
- **Automated test execution** (currently skipped; should be mandatory for production workflows)
- **Push to Docker Hub** for image distribution
- **Deployment trigger** to cloud hosting platform

### Recommended Production Workflow

For production-grade CI/CD:

- ✅ Make test execution mandatory before image publication
- ✅ Add security scanning (dependency vulnerabilities, SAST analysis)
- ✅ Implement deployment approvals for production environments
- ✅ Add performance regression testing for critical paths

## Scalability Roadmap

The current architecture is intentionally conservative. The following improvements fit naturally into the design and can be prioritized based on actual operational requirements:

### High Priority (Immediate Value)

- **Introduce schema migrations:** Replace `ddl-auto=update` with Flyway or Liquibase for safe, reproducible schema evolution
- **Externalize JWT secret management:** Use environment-based secret injection or dedicated secret management services
- **Add explicit role and permission modeling:** Extend ownership-based auth to support team-based access control and fine-grained RBAC if needed

### Medium Priority (Operational Excellence)

- **Integrate Redis for caching:** Add caching layer for leaderboard and consistency read models—reduces database queries on high-traffic reads
- **Add pagination and query optimization:** Implement cursor-based pagination and database query analysis for read-heavy aggregation endpoints
- **Introduce audit fields:** Add `created_by`, `created_at`, `updated_by`, `updated_at` timestamps for compliance and debugging

### Lower Priority (Future Scaling)

- **Extract execution module as service:** If execution logging becomes independent scaling bottleneck, extract as separate Spring Boot application with event-driven sync
- **Implement observability layer:** Structured logging, distributed tracing (OpenTelemetry), and metrics collection for multi-service deployments
- **Add async workflow boundaries:** If payment or notification side effects justify asynchronicity, introduce message queue integration

## Production Readiness Checklist

Before deploying to production users:

- [ ] Replace `spring.jpa.hibernate.ddl-auto=update` with Flyway migrations
- [ ] Externalize all secrets (JWT secret, database credentials, API keys)
- [ ] Enable HTTPS and secure communication channels
- [ ] Implement rate limiting and DDoS protection
- [ ] Set up centralized logging and monitoring
- [ ] Define backup and disaster recovery procedures
- [ ] Conduct security audit and penetration testing
- [ ] Document runbooks for common operational scenarios
- [ ] Set up alerting for critical application and infrastructure metrics

## License

This project is licensed under the [MIT License](LICENSE).
