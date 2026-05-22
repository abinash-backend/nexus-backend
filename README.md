# Nexus | Workflow Execution Platform

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-brightgreen?style=for-the-badge&logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring_Security-JWT%20Secured-6DB33F?style=for-the-badge&logo=springsecurity)
![JWT](https://img.shields.io/badge/Auth-JWT-blue?style=for-the-badge&logo=jsonwebtokens)
![Database](https://img.shields.io/badge/Database-PostgreSQL-4169E1?style=for-the-badge&logo=postgresql)
![Build](https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apachemaven)
![API Docs](https://img.shields.io/badge/API-Swagger%20%2F%20OpenAPI-85EA2D?style=for-the-badge&logo=swagger)
![Testing](https://img.shields.io/badge/Tests-JUnit5%20%7C%20Mockito%20%7C%20MockMvc-25A162?style=for-the-badge)
![Docker](https://img.shields.io/badge/Container-Docker-2496ED?style=for-the-badge&logo=docker)

---
## Live API

Swagger API Documentation  
[Open Swagger UI](https://execution-os-backend-latest.onrender.com/swagger-ui/index.html)

System Health Endpoint  
[Check API Health](https://execution-os-backend-latest.onrender.com/api/system/health)

GitHub Repository  
[GitHub Repository](https://github.com/abinash-backend/execution-os-backend)

---
## About

**Nexus | Workflow Execution Platform** is a Spring Boot 3 REST API for personal execution tracking. It provides secure APIs for user authentication, task management, daily execution logging, streak analytics, and leaderboard generation.

The project follows a **modular monolith** approach with clear domain separation and layered backend structure. It is designed as a backend-first system with JWT-based security, PostgreSQL persistence, Swagger documentation, test coverage, and containerized local deployment.

---

## Architecture Overview

The application is organized as a **layered modular monolith**, where each business capability is grouped into its own package while remaining inside one deployable Spring Boot application.

### Core Architectural Principles

- Domain-oriented package structure
- Layered separation of controller, service, repository, and persistence concerns
- Shared infrastructure for security, configuration, and exception handling
- Stateless JWT authentication
- API-first REST design

### Layered Structure

`Controller -> Service -> Repository -> Database`

- `Controller` handles HTTP requests and response mapping
- `Service` contains business logic and orchestration
- `Repository` handles data access through Spring Data JPA
- `Database` persists application state in PostgreSQL

---

## Domain Modules

| Module | Responsibility |
| --- | --- |
| `auth` | User registration, login, password encoding, and JWT issuance |
| `task` | Task creation, retrieval, filtering, streak calculation, and leaderboard computation |
| `execution` | Daily execution logging, duplicate-per-day prevention, and execution history retrieval |
| `common` | Shared configuration, JWT security, exception handling, and utility enums |
| `system` | Root and health endpoints for service availability checks |

---

## System Diagram

```text
                          +------------------------------+
                          |        Client Apps           |
                          |   Web / Mobile / Postman     |
                          +--------------+---------------+
                                         |
                                         v
                          +------------------------------+
                          |  Spring Security + JWT Filter|
                          +--------------+---------------+
                                         |
                                         v
                          +------------------------------+
                          |      REST Controllers        |
                          +--------------+---------------+
                                         |
                                         v
        +-------------------------------------------------------------------+
        |                         Service Layer                             |
        |      auth      |      task      |    execution    |    common     |
        +----------------+----------------+-----------------+---------------+
                                         |
                                         v
                          +------------------------------+
                          |   Spring Data JPA Repos      |
                          +--------------+---------------+
                                         |
                                         v
                          +------------------------------+
                          |      PostgreSQL Database     |
                          +------------------------------+
```

---

## Request Flow

### Task Creation Flow

```text
Client
  |
  v
JWT Authenticated Request
  |
  v
Task Controller
  |
  v
Task Service
  |
  +--> Resolve Authenticated User
  |
  +--> Validate Duplicate Task Title
  |
  +--> Build Task
  |
  +--> Persist Task
  |
  v
Task Repository
  |
  v
PostgreSQL Database
  |
  v
API Response
```

### Execution Logging Flow

```text
User Request -> JWT Validation -> Execution Controller -> Execution Service
             -> Validate Task Ownership -> Prevent Same-Day Duplicate Log
             -> Save Execution Log -> Return Response
```

---

## Tech Stack

| Category | Technologies |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Security | Spring Security, JWT Authentication |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL |
| Build Tool | Maven Wrapper |
| API Documentation | Swagger / OpenAPI via Springdoc |
| Testing | JUnit 5, Mockito, MockMvc |
| Containerization | Docker, Docker Compose |

---

## Features

### Core Features

- User registration and login
- JWT-based stateless authentication
- Protected task and execution APIs
- Create tasks for authenticated users
- Retrieve tasks with optional `status` and `priority` filters
- Track daily execution logs
- Prevent duplicate execution logs for the same task on the same day
- Calculate streak and consistency metrics
- Generate user consistency leaderboard

### Backend Engineering Features

- Global exception handling
- DTO-based API contracts
- Controller layer tests with MockMvc
- Service layer unit tests with Mockito
- Swagger UI integration
- Multi-stage Docker build
- Docker Compose setup with PostgreSQL health checks

---

## Swagger Docs

Swagger UI is available at:

`http://localhost:8080/swagger-ui.html`

OpenAPI JSON is available at:

`http://localhost:8080/v3/api-docs`

---


## Docker Setup

The project includes:

- Multi-stage `Dockerfile`
- `docker-compose.yml` for backend + PostgreSQL
- Persistent PostgreSQL volume
- Database health check using `pg_isready`
- Spring datasource configuration through environment variables
- PostgreSQL exposed on host port `5433`

Run the full system with:

```bash
docker compose up --build
```

Stop it with:

```bash
docker compose down
```

---

## Project Structure

```text
execution-os-backend/
|-- .mvn/
|-- src/
|   |-- main/
|   |   |-- java/
|   |   |   `-- com/
|   |   |       `-- executionos/
|   |   |           |-- auth/
|   |   |           |-- common/
|   |   |           |-- execution/
|   |   |           |-- system/
|   |   |           |-- task/
|   |   |           `-- ExecutionOsBackendApplication.java
|   |   `-- resources/
|   |       `-- application.yaml
|   `-- test/
|       `-- java/
|           `-- com/
|               `-- executionos/
|                   |-- auth/controller/
|                   |-- execution/controller/
|                   |-- execution/service/
|                   |-- task/controller/
|                   `-- task/service/
|-- .dockerignore
|-- Dockerfile
|-- docker-compose.yml
|-- pom.xml
|-- mvnw
|-- mvnw.cmd
`-- README.md
```

---

## Getting Started

### Prerequisites

- Java 17
- PostgreSQL
- Docker Desktop or Docker Engine

### Clone Repository

```bash
git clone <repository-url>
cd execution-os-backend
```

### Build the Project

```bash
./mvnw clean package
```

On Windows:

```powershell
.\mvnw.cmd clean package
```

### Run the Application

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

---

## Configuration

The application supports local file-based configuration and containerized environment-variable configuration.

### Local Defaults

Current defaults in `application.yaml`:

| Variable | Default |
| --- | --- |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/execution_os` |
| `spring.datasource.username` | `execution_user` |
| `spring.datasource.password` | `secure123` |
| `spring.jpa.hibernate.ddl-auto` | `update` |
| `spring.jpa.show-sql` | `true` |
| `server.port` | `8080` |

### Docker Environment Variables

The Docker Compose setup injects:

| Variable | Value |
| --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/execution_os` |
| `SPRING_DATASOURCE_USERNAME` | `execution_user` |
| `SPRING_DATASOURCE_PASSWORD` | `secure123` |

---

## API Snapshot

### Auth Endpoints

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`

### Task Endpoints

- `POST /api/v1/tasks`
- `GET /api/v1/tasks`
- `GET /api/v1/tasks/{taskId}/streak`
- `GET /api/v1/tasks/leaderboard`

### Execution Endpoints

- `POST /api/v1/tasks/{taskId}/execution`
- `GET /api/v1/tasks/{taskId}/execution`

---

## Security

- All endpoints except `/api/v1/auth/**` require authentication
- JWT bearer tokens are validated by a custom `JwtFilter`
- Authenticated user identity is derived from the token subject
- Cross-user task execution is rejected at service level

Example header:

```http
Authorization: Bearer <jwt-token>
```

---

## Testing

The project includes:

- Service-layer unit tests for `TaskServiceImpl`
- Service-layer unit tests for `ExecutionService`
- Controller-layer `MockMvc` tests for:
  - `AuthController`
  - `TaskController`
  - `ExecutionController`

Run tests with:

```bash
./mvnw test
```

On Windows:

```powershell
.\mvnw.cmd test
```

---

## CI Pipeline

The repository includes a GitHub Actions workflow at `.github/workflows/docker-ci.yml`.

Pipeline flow:

`Local Development -> GitHub Repository -> GitHub Actions -> Maven Build/Test -> Docker Build -> Docker Hub -> Render`

On every push to `main`, GitHub Actions will:

1. Checkout the repository
2. Set up Java 17 with Maven dependency caching
3. Run `./mvnw -B clean package -DskipTests`
4. Build the Docker image from the root `Dockerfile`
5. Log in to Docker Hub using repository secrets
6. Push the image as `<dockerhub-username>/execution-os-backend:latest`
7. Trigger a Render deploy hook

### Required GitHub Secrets

Configure these repository secrets under `Settings -> Secrets and variables -> Actions`:

| Secret | Purpose |
| --- | --- |
| `DOCKER_USERNAME` | Docker Hub username |
| `DOCKER_PASSWORD` | Docker Hub password or access token |
| `RENDER_DEPLOY_HOOK` | Render deploy hook URL |

### Render Deployment From Docker Hub

To deploy this backend on Render using the Docker Hub image:

1. Push code to the `main` branch so GitHub Actions publishes the image
2. Open Render and create a new `Web Service`
3. Choose `Deploy an existing image from a registry`
4. Set the image to `<dockerhub-username>/execution-os-backend:latest`
5. Set the service port to `8080`
6. Add required environment variables such as:
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
   - `SPRING_JPA_HIBERNATE_DDL_AUTO`
   - `PORT=8080`
7. Deploy the service and enable auto-deploy or manual redeploys as needed when a new image is pushed

Render will pull the latest Docker image from Docker Hub and start the container with:

`java -jar /app/app.jar`

---

## Future Improvements

- Externalize the JWT secret into environment configuration
- Add Flyway or Liquibase database migrations
- Add update and delete task endpoints
- Add ownership checks for every task-derived analytics endpoint
- Introduce profile-based production configuration

---

## Author

**Abinash Nayak**  
Java Backend Developer  
GitHub: [Aj-world](https://github.com/Aj-world)
