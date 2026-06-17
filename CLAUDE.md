# LubeLog

LubeLog is a vehicle maintenance tracker API. This is a learning project and portfolio piece. **Backend quality is the priority.** Frontend will be handled separately later.

---

## Project Purpose

Help users track vehicle service history and upcoming maintenance. Users can have many vehicles, app will be useful for individuals and businesses with fleets of vehicles.

---

## Tech Stack

| Layer            | Technology                                                |
| ---------------- | --------------------------------------------------------- |
| Language         | Java (Spring Boot)                                        |
| Database         | PostgreSQL                                                |
| Migrations       | Flyway (owns schema — Hibernate DDL auto is **disabled**) |
| Auth             | Keycloak (self-hosted, OAuth2 + Google login)             |
| Containerization | Docker + Docker Compose                                   |
| CI/CD            | GitHub Actions → SSH deploy to VPS                        |
| VIN Lookup       | NHTSA vPIC API (free, no key required)                    |
| API Docs         | Springdoc (OpenAPI / Swagger UI)                          |
| Testing          | JUnit + Mockito + Testcontainers                          |
| Monitoring       | Spring Actuator                                           |

---

## Package Structure

Root package: `dev.jacksonfishburn.lubelog`

Standard Maven layout: `src/main/java/dev/jacksonfishburn/lubelog/`

Typical subpackages:

- `controller` — REST controllers
- `service` — business logic
- `repository` — Spring Data JPA repositories
- `entity` — JPA entities
- `dto` — request/response DTOs (keep separate from entities)
- `config` — Spring config classes (security, CORS, etc.)
- `exception` — custom exceptions and global exception handler

---

## Database Schema

**users** — local reference to Keycloak user

- `id`, `keycloak_id`, `email`, `created_at`

**vehicles**

- `id`, `user_id`, `year`, `make`, `model`, `trim`, `vin`, `nickname`, `mileage`, `created_at`

**service_types** — global defaults + user-created custom types

- `id`, `user_id` (NULL for globals), `name`, `is_global`, `created_at`
- Global defaults are seeded at startup (oil change, tire rotation, etc.)

**vehicle_services** — per-vehicle service configuration

- `id`, `vehicle_id`, `service_id`, `interval_miles`, `interval_months`, `created_at`

**service_logs**

- `id`, `vehicle_service_id`, `done_at_mileage`, `done_at_date`, `cost`, `notes`, `created_at`

**service_log_details** — flexible key/value pairs for parts/notes

- `id`, `service_log_id`, `key`, `value`

---

## Key Design Decisions

- **Flyway owns the schema.** Never use `spring.jpa.hibernate.ddl-auto=create` or `update`. All schema changes go through versioned Flyway migrations.
- **Service interval is per vehicle, not per service type.** `service_types` holds a suggested default. `vehicle_services` holds the actual interval for that specific vehicle. This lets the same service type (e.g. Oil Change) have different intervals on different vehicles.
- **VIN lookup is best-effort.** Proxies NHTSA vPIC API. If it returns no result, the user fills in vehicle info manually.
- **`service_log_details` uses flexible key/value pairs**, not fixed columns. New service types don't require schema changes.
- **App never handles passwords or OAuth handshakes.** Keycloak owns all auth. Spring Boot only validates JWTs issued by Keycloak.
- **Single database** shared by Keycloak and the backend (kept simple for this stage).

---

## Auth

- Keycloak runs as a Docker container alongside the app
- Supports "Sign in with Google" via Keycloak
- Spring Boot validates JWTs using Keycloak's JWKS endpoint
- The `users` table stores a local record keyed to `keycloak_id` (the `sub` claim)
- All API endpoints require a valid JWT unless explicitly public


---

## Coding Conventions

Follow standard Spring Boot best practices:

- Controllers are thin — delegate to service layer
- Services contain all business logic
- Repositories are Spring Data JPA interfaces; avoid native queries unless necessary
- Use DTOs for all request/response bodies; never expose entities directly
- Use `@RestControllerAdvice` for global exception handling
- Prefer constructor injection over field injection
- Validate request bodies with Jakarta Bean Validation (`@Valid`, `@NotNull`, etc.)
- Return meaningful HTTP status codes (`201 Created`, `404 Not Found`, etc.)

---

## What's Done

- Spring Boot project initialized
- Docker Compose set up (app, PostgreSQL, Keycloak — single shared DB)
- Flyway migrations created
- JPA entities and repositories created to match migrations

---

## Build Order (Remaining)


1. Vehicle CRUD + VIN lookup
2. Keycloak Spring Boot integration (JWT validation, user sync)
3. Service types (seed global defaults)
4. Vehicle services (per-vehicle config)
5. Service logs + log details
6. Upcoming/reminders calculation
7. CORS + rate limiting (Bucket4j)
8. Springdoc / Swagger UI
9. GitHub Actions CI/CD pipeline

---