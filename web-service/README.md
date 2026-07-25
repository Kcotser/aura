# Aura Backend

Multimodal personal safety platform for women in harassment situations.
Built as a modular monolith using **Spring Boot 4.1.0**, **Java 21**, **MongoDB Atlas**, and **DDD + Hexagonal Architecture**.

## Architecture

See [ARCHITECTURE.md](./ARCHITECTURE.md) for a detailed Architecture Decision Record.

The system is organized as 8 bounded contexts under `com.aura`:

| Context | Package | Status |
|---|---|---|
| Identity & Access Management | `com.aura.iam` | ✅ Complete |
| Emergency Activation | `com.aura.emergencyactivation` | 🚧 Scaffolded |
| Evidence | `com.aura.evidence` | 🚧 Scaffolded |
| Analysis (Gemma 4) | `com.aura.analysis` | 🚧 Scaffolded |
| Reporting | `com.aura.reporting` | 🚧 Scaffolded |
| Trust Network | `com.aura.trustnetwork` | 🚧 Scaffolded |
| Institutional Directory | `com.aura.directory` | 🚧 Scaffolded |
| Privacy | `com.aura.privacy` | 🚧 Scaffolded |

## Prerequisites

- Java 21+
- Maven 3.9+
- Docker (for integration tests via Testcontainers)
- MongoDB Atlas cluster (or local MongoDB for dev)

## Running Locally (dev profile)

The `dev` profile uses a locally configured MongoDB URI and a fixed JWT secret safe for development.

```bash
# Clone and enter the project
cd web-service

# Run with dev profile (default)
mvn spring-boot:run

# The API will be available at http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui/index.html
# OpenAPI JSON: http://localhost:8080/v3/api-docs
```

### Dev Configuration

The `application.yml` includes sensible defaults for local development:
- MongoDB: `mongodb://localhost:27017/aura_dev`
- JWT secret: fixed dev-only secret (do NOT use in production)
- Virtual threads: enabled

## Running Tests

```bash
# Run all tests (unit + integration)
# Requires Docker running for Testcontainers (MongoDB)
mvn test

# Run only unit tests (no Docker needed)
mvn test -Dgroups=unit

# Run only integration tests
mvn test -Dgroups=integration
```

The test suite includes:
1. **Spring Modulith verification** (`ApplicationModularityTests`) — fails if any module imports internal classes of another module
2. **IAM unit tests** — domain model and use case logic
3. **IAM integration tests** — full register → login → refresh flow against a real MongoDB (Testcontainers)

## Deploying to Production (MongoDB Atlas)

1. Copy `.env.example` to `.env` and fill in all values
2. Set environment variables in your deployment environment (never commit `.env`)
3. Build the production JAR:

```bash
mvn clean package -DskipTests
```

4. Run with the `prod` profile:

```bash
java -jar target/aura-backend-0.1.0-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

Or with Docker:

```dockerfile
# Example Dockerfile
FROM eclipse-temurin:21-jre-alpine
ARG JAR_FILE=target/aura-backend-0.1.0-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## API Endpoints (IAM)

| Method | Path | Description | Auth |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Register new user | Public |
| POST | `/api/v1/auth/login` | Login (returns JWT) | Public |
| POST | `/api/v1/auth/refresh` | Renew access token | Public |
| POST | `/api/v1/auth/logout` | Revoke session | Bearer |
| POST | `/api/v1/auth/pin` | Configure/update PIN | Bearer |
| POST | `/api/v1/auth/pin/validate` | Validate PIN | Bearer |
| GET | `/api/v1/users/me` | Get profile | Bearer |
| PATCH | `/api/v1/users/me` | Update profile | Bearer |
| DELETE | `/api/v1/users/me` | Request account deletion | Bearer |

## Security Notes

- Passwords and PINs are always hashed with BCrypt — never stored or logged in plain text
- Access tokens expire in 15 minutes (configurable)
- Refresh tokens are stored **hashed** in MongoDB — the raw token is only returned once
- Rate limiting on login and PIN validation endpoints (Bucket4j, in-memory)
- CORS configured for production origin only
```
