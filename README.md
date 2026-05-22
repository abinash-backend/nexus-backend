# Nexus | Workflow Execution Platform

![Java 17](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-brightgreen?style=flat-square&logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-6DB33F?style=flat-square&logo=springsecurity)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=flat-square&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=flat-square&logo=apachemaven)
![OpenAPI](https://img.shields.io/badge/API-OpenAPI-85EA2D?style=flat-square&logo=swagger)
![Architecture](https://img.shields.io/badge/Architecture-Modular%20Monolith-333333?style=flat-square)

Nexus is a backend platform for workflow execution, task orchestration, and execution lifecycle tracking. It is implemented as a Spring Boot modular monolith with explicit domain boundaries so the codebase can scale operationally today and be decomposed into services later without forcing an early distributed systems design.

The current repository focuses on secure API delivery, workflow accountability, and predictable state handling inside a single deployable unit. The architecture is backend-first: REST APIs, relational persistence, stateless authentication, containerized runtime, and domain separation that supports long-term maintainability.

## Overview

Nexus models workflow execution as a controlled lifecycle:

1. An authenticated user registers or signs in and receives a JWT.
2. The user creates tasks that become workflow units owned by that identity.
3. Execution events are recorded against those tasks on a per-day basis.
4. The platform enforces ownership and duplicate-execution guards.
5. Read models expose task state, execution history, and consistency metrics.

This keeps the platform centered on operational accountability rather than simple CRUD. The main design goal is reliable workflow state evolution under a clear module structure.

## Architecture

### Architectural direction

- Modular monolith
- Domain-driven module separation
- Layered application structure
- Single-database consistency model
- REST API boundary for future service extraction

### Why a modular monolith

Nexus is intentionally kept as one deployable unit while separating business capabilities into domain modules. That choice keeps operational complexity low in the current stage while preserving extraction seams for future services such as `auth`, `task`, `execution`, or analytics-oriented read workloads.

This is not presented as a microservices platform today. The repository is structured to make that transition feasible later, not to claim distributed guarantees that do not exist.

### Module boundaries

| Module | Responsibility |
| --- | --- |
| `auth` | Registration, login, password hashing, and JWT issuance |
| `task` | Task creation, task retrieval, filters, streak computation, and leaderboard assembly |
| `execution` | Execution logging, per-task execution history, and execution idempotency constraints |
| `common` | Shared security, OpenAPI configuration, exception handling, and cross-cutting utilities |
| `system` | Health and service availability endpoints |

### Layered flow

`Controller -> Service -> Repository -> PostgreSQL`

Each module follows a conventional layered path:

- controllers define HTTP contracts
- services coordinate workflow rules
- repositories isolate persistence access
- entities and DTOs keep domain and transport concerns separated

## Workflow Execution Model

The workflow subsystem is built around task ownership and execution traceability.

- A task belongs to a single user.
- Execution logs are recorded against a task and a calendar date.
- A composite database uniqueness constraint prevents duplicate execution entries for the same task on the same day.
- Service-layer ownership checks reject cross-user access to workflow state.
- Consistency metrics are derived from persisted execution history rather than transient in-memory state.

That model gives the platform a clear audit trail for day-to-day operational activity while staying simple enough to evolve inside a monolith.

## Feature Highlights

- Stateless JWT authentication for protected APIs
- User-scoped task creation and retrieval
- Execution lifecycle logging per task
- Ownership enforcement on workflow access paths
- Duplicate execution prevention at both service and database layers
- Streak and consistency calculations from persisted execution history
- Leaderboard-style aggregation for user consistency scoring
- OpenAPI documentation for API consumers
- Containerized runtime with Docker and Docker Compose
- Actuator-backed health exposure for operational monitoring

## Tech Stack

| Category | Technology |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security, JWT |
| Validation | Jakarta Bean Validation |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL |
| Caching | Redis is part of the intended runtime roadmap, but not wired in the current repository revision |
| API Documentation | Springdoc OpenAPI / Swagger UI |
| Build | Maven Wrapper |
| Containers | Docker, Docker Compose |
| Testing | JUnit 5, Mockito, MockMvc |

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

Security is currently centered on stateless API protection and resource ownership enforcement.

- Spring Security is configured for stateless request handling.
- JWTs are issued on successful authentication and validated through a custom filter.
- Protected endpoints require an authenticated principal.
- Passwords are stored with BCrypt hashing.
- Workflow resources are protected with service-layer ownership checks using the authenticated user identifier.
- OpenAPI is configured with bearer authentication support for interactive testing.

### Authorization note

This codebase currently implements JWT authentication plus ownership-based authorization. It does not yet include a full RBAC matrix, role entity model, or policy layer. Role-based operational workflows are an appropriate next step, but they should be added explicitly rather than implied.

## Database and Transactional Consistency

Nexus uses PostgreSQL as the system of record and keeps write consistency inside a single relational boundary.

- Task and execution state live in the same database, which keeps workflow updates synchronous and predictable.
- Execution logging uses a composite unique constraint on `task_id` and `date` to prevent duplicate same-day execution records.
- The service layer performs application-level validation before persistence and still relies on the database as the final consistency guard.
- The current design favors single-node transactional integrity over distributed coordination.

### Production note

The repository currently uses `spring.jpa.hibernate.ddl-auto=update` for convenience. A production rollout should replace that with explicit schema migrations through Flyway or Liquibase before promoting the service to a controlled environment.

## Redis Caching Strategy

Redis is part of the platform's intended operational design, but it is not integrated in the current codebase yet.

The natural fit for Redis in Nexus would be:

- caching leaderboard or consistency read models
- short-lived workflow aggregation results
- token revocation or session invalidation support
- rate-limiting or coordination primitives for higher write concurrency

That makes Redis a planned acceleration layer, not a correctness dependency. PostgreSQL remains the source of truth.

## API Documentation

Swagger UI and OpenAPI are enabled through Springdoc.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health endpoint: `http://localhost:8080/api/system/health`

### Primary API surface

| Area | Endpoints |
| --- | --- |
| Auth | `POST /api/v1/auth/register`, `POST /api/v1/auth/login` |
| Tasks | `POST /api/v1/tasks`, `GET /api/v1/tasks` |
| Execution | `POST /api/v1/tasks/{taskId}/execution`, `GET /api/v1/tasks/{taskId}/execution` |
| Metrics | `GET /api/v1/tasks/{taskId}/streak`, `GET /api/v1/tasks/leaderboard` |
| System | `GET /api/system/health` |

## Docker Setup

The repository includes:

- a multi-stage `Dockerfile`
- a `docker-compose.yml` for the backend and PostgreSQL
- a PostgreSQL health check using `pg_isready`
- a non-root runtime container user
- environment-driven datasource wiring for containers

### Start the stack

```bash
docker compose up --build
```

### Stop the stack

```bash
docker compose down
```

### Container endpoints

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- PostgreSQL host port: `5433`

## Local Development

### Prerequisites

- Java 17
- Maven wrapper support
- PostgreSQL 15+ or a compatible local PostgreSQL instance
- Docker Desktop or Docker Engine for containerized runs

### Build

```bash
./mvnw clean package
```

Windows PowerShell:

```powershell
.\mvnw.cmd clean package
```

### Run locally

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

### Run tests

```bash
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
```

## Environment Configuration

The application reads configuration from environment variables with local defaults in `src/main/resources/application.yaml`.

| Variable | Purpose | Default |
| --- | --- | --- |
| `PORT` | HTTP server port | `8080` |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/execution_os` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `execution_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `secure123` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate schema strategy | `update` |

### Operational recommendation

For non-local environments:

- move all secrets into environment-managed configuration
- replace inline development credentials
- externalize JWT signing secrets
- disable verbose SQL logging
- pin schema changes through migrations

## CI/CD

The repository includes GitHub Actions workflows under `.github/workflows/`.

Current delivery automation includes:

- Maven build on push to `main`
- Docker image build and push to Docker Hub
- Render deploy hook trigger after image publication

The present CI pipeline builds with `-DskipTests`. For a production release workflow, test execution should be mandatory before image publication.

## Scalability Roadmap

Near-term engineering improvements that fit the current architecture:

- introduce Flyway or Liquibase migrations
- externalize JWT secret management
- add explicit role and permission modeling for RBAC
- integrate Redis for hot-path read caching and operational controls
- add pagination and query optimization for read-heavy aggregation endpoints
- introduce audit fields and richer workflow state transitions
- extract `execution` and analytics-oriented read paths once independent scaling pressure exists

## Engineering Positioning

Nexus is best understood as a scalable backend foundation rather than a feature-maximal workflow suite. Its value is in the system shape: clear domain boundaries, secure request handling, relational consistency, and a codebase that can mature toward stronger operational requirements without a rewrite.
