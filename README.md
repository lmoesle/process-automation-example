# Miravelo Urlaubsantrag

## Purpose

This project showcases how to automate a BPMN-based business process in a Spring Boot application.
The example use case is a vacation approval workflow with:

- submission of a vacation request
- an automatic validity check
- a manager approval step
- notification paths for approved and declined requests

The repository is intended as a small, concrete reference for combining process modeling with executable process automation.

## Technical Setup

- Spring Boot 3.5.7
- Camunda 7.24 embedded in the application
- Process Engine API and vanilla Camunda 7 backend variations
- PostgreSQL 18 provided via Docker Compose
- Flyway database migrations
- BPMN-to-Java code generation from files in each backend's `src/main/resources/bpmn` directory

## Repository Structure

```text
pom.xml      Maven aggregator for all backend variants
apps/
  c7-process-engine-api/  Backend using the Process Engine API
  c7-vanilla/             Backend using the vanilla Camunda 7 APIs
  frontend/               Vite React frontend
stack/                    Infrastructure and full-stack Docker Compose files
```

## Prerequisites

- Java 25
- Node.js 24 with npm
- Docker with Docker Compose support
- GNU Make

The Maven build is configured with `source` and `target` level `25`. If you build with an older JDK, Maven will fail with `invalid target release: 25`.

## Build The Project

Use the Maven wrapper from the repository root to build all backend variants registered in the root aggregator:

```bash
./mvnw clean verify
```

This build runs the BPMN code generation, compiles the application, executes the tests, and packages the Spring Boot application.

## Start The Infrastructure Stack

The repository contains a Docker Compose file for PostgreSQL and MailHog:

```bash
docker compose -f stack/docker-compose.yml up -d
```

To check the stack status:

```bash
docker compose -f stack/docker-compose.yml ps
```

To stop the stack again:

```bash
docker compose -f stack/docker-compose.yml down
```

The database is exposed on `localhost:5432` with these default settings:

- database: `vacation-approval`
- user: `user`
- password: `secret`

## Run The Application

After the infrastructure is up, start either Spring Boot backend:

```bash
./mvnw -pl apps/c7-process-engine-api spring-boot:run
# or
./mvnw -pl apps/c7-vanilla spring-boot:run
```

By default, the application connects to the PostgreSQL container started through Docker Compose.

Camunda admin credentials are configured with these defaults:

- username: `admin`
- password: `admin`

After startup, the Camunda web application is available at:

```text
http://localhost:8080/camunda/app/
```

The OpenAPI description and Swagger UI are available at:

```text
http://localhost:8080/v3/api-docs
http://localhost:8080/swagger-ui/index.html
```

## Run A Full Stack With Docker Compose

The Makefile builds the application artifacts and local Docker images before starting a stack. Show all available targets with:

```bash
make help
```

Build and start the Process Engine API variation:

```bash
make full-c7-process-engine-api
```

Build and start the vanilla Camunda 7 variation:

```bash
make full-c7-vanilla
```

The variations use separate Compose projects, networks, and PostgreSQL volumes because their Flyway migrations differ. They also use distinct host ports and can run concurrently:

| Service | Process Engine API | Vanilla Camunda 7 |
| --- | --- | --- |
| Frontend | http://localhost:3000 | http://localhost:3001 |
| Backend | http://localhost:8080 | http://localhost:8081 |
| Camunda | http://localhost:8080/camunda/app/ | http://localhost:8081/camunda/app/ |
| PostgreSQL | localhost:5432 | localhost:5433 |
| MailHog SMTP | localhost:1025 | localhost:1026 |
| MailHog UI | http://localhost:8025 | http://localhost:8026 |

All ports are bound to the local loopback interface. The full stacks are alternatives to running the same services directly on the host. Stop processes that already use the listed ports first; in particular, run `make infrastructure-down` before starting the Process Engine API full stack.

Use the `-up` targets to start already-built images, the `-logs` targets to follow application logs, and the `-down` targets to stop a stack:

```bash
make full-c7-process-engine-api-up
make full-c7-process-engine-api-logs
make full-c7-process-engine-api-down
```

The `-down` targets preserve PostgreSQL data. Use the explicit `-reset` target to stop a stack and delete its database volume:

```bash
make full-c7-process-engine-api-reset
```

After building the local images, the Compose files can also be used directly:

```bash
docker compose -f stack/docker-compose.c7-process-engine-api.yml up -d
docker compose -f stack/docker-compose.c7-vanilla.yml up -d
```
