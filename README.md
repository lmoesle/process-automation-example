# Process Automation Example

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
- Process-Engine-API
- PostgreSQL 18 provided via Docker Compose
- Flyway database migrations
- BPMN-to-Java code generation from files in `src/main/resources/bpmn`

## Prerequisites

- Java 25
- Docker with Docker Compose support

The Maven build is configured with `source` and `target` level `25`. If you build with an older JDK, Maven will fail with `invalid target release: 25`.

## Build The Project

Use the Maven wrapper from the repository root:

```bash
./mvnw clean verify
```

This build runs the BPMN code generation, compiles the application, executes the tests, and packages the Spring Boot application.

## Start The Infrastructure Stack

The repository contains a Docker Compose file for PostgreSQL:

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

After the database is up, start the Spring Boot application:

```bash
./mvnw spring-boot:run
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
