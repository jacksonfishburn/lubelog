# LubeLog — Project Overview

> **Maintenance note:** This document is the source of truth for the *current* state of the
> project and is meant to be handed to an AI for design discussions. Keep it accurate as the
> project evolves — see the maintenance rule in `CLAUDE.md`. Last verified against the codebase
> on **2026-07-03**.

LubeLog is a vehicle maintenance tracker — a learning project and portfolio piece. Users log
service history, configure per-vehicle service intervals, and (will) get upcoming maintenance
reminders. It supports individuals and small fleets. **Backend quality is the priority;** the
frontend is currently wired to the real backend API (via Vite proxy) with Keycloak login.

---

## Purpose

- Track vehicle service history and surface upcoming/overdue maintenance.
- Support individuals and small fleets (e.g. a company with ~50 vehicles).
- Built to production-grade standards as a deliberate learning exercise in backend design and
  industry-standard tooling.

---

## Tech Stack (as built)

| Layer            | Technology                                                        |
| ---------------- | ----------------------------------------------------------------- |
| Backend API      | Java 21 + Spring Boot 3.5.15                                       |
| Database         | PostgreSQL 16 (everything lives in the `app` schema)              |
| Migrations       | Flyway (owns the schema; `ddl-auto: validate`)                    |
| Auth             | Keycloak 26.2 (OAuth2 / Google login); backend is a resource server validating JWTs |
| VIN Lookup       | NHTSA vPIC API (free, no key)                                     |
| Containerization | Docker + Docker Compose                                           |
| Testing          | JUnit + Mockito + Testcontainers (Postgres) + spring-security-test |
| Monitoring       | Spring Boot Actuator (dependency present; `/actuator/**` is public) |
| Rate limiting    | Bucket4j (`bucket4j_jdk17-core`) — in-memory, per-user token bucket over `/api/**` |
| Frontend         | React 19 + Vite + TypeScript; Keycloak-js (PKCE) + authenticated `apiFetch` client |

**Not yet added** (planned): CORS config, GitHub Actions CI/CD, Redis.

### Ports

| Service          | Port  |
| ---------------- | ----- |
| Backend          | 8081  |
| Keycloak         | 8080  |
| PostgreSQL       | 5432  |
| Frontend (Vite)  | 5173  |

---

## Current Status

**Done:**

- Schema + Flyway migrations (V1–V7), all in the `app` schema.
- Keycloak + Spring Boot resource-server auth: JWT validation, automatic local-user
  provisioning, current-user resolution.
- Vehicle CRUD + VIN lookup (NHTSA vPIC proxy).
- Service types: global defaults seeded at startup + user custom types, with duplicate guard
  and a guard against deleting globals.
- Vehicle services (per-vehicle service configuration with intervals).
- Service logs + flexible key/value log details, including auto-updating the vehicle's mileage
  from logged service mileage.
- Next-due reminder calculation (computed on the fly — see below).
- Environment-based config profiles (`dev` for native local runs vs. default/containerized).
- Established vertical-slice pattern with integration + unit test coverage.
- Springdoc / Swagger UI.
- React Frontend
- GitHub Actions CI/CD pipeline.
- Email notifications for upcoming/overdue services; scheduled reminders.
- Rate limiting (Bucket4j) — global per-user token bucket over `/api/**`.

**Up next** (no particular order):

- AI-assisted parts lookup per service type.
- Redis caching, full monitoring stack (Grafana + Prometheus), cost analytics,
  multi-user vehicle sharing.

---

## Architecture

### Vertical slice pattern

Every feature follows the same slice, and new work should too:

```
entity → repository → service → controller → DTOs → custom exceptions → integration tests
```

Guiding rules (from `CLAUDE.md`):

- **Add, don't modify.** Prefer new classes/methods over changing working code. Each addition
  has one clear responsibility.
- **No clever abstractions / premature generalization.**
- **Never modify existing Flyway migrations** — new schema changes get a new migration file.
- **Hibernate DDL auto is disabled** (`validate`). Flyway owns the schema; JPA never alters it.

### Package layout

`dev.jacksonfishburn.lubelog`

- `entity` — `User`, `Vehicle`, `ServiceType`, `VehicleService`, `ServiceLog`, `ServiceLogDetail`, `ServiceReminder`
- `repository` — Spring Data JPA repositories
- `service` — business logic (`VehicleService`, `ServiceTypeService`, `VehicleServiceService`, `ServiceLogService`, `UserService`, `ReminderEmailService`, `ReminderService`)
- `controller` — REST controllers
- `dto` — request/response records
- `client` — `VinClient` (NHTSA vPIC integration)
- `security` — `SecurityConfig`, `UserProvisioningFilter`, `AuthUtils`, `RateLimitFilter`, `RateLimitBucketStore`
- `config` — `OpenApiConfig`, `RateLimitProperties` (`@ConfigurationProperties(prefix = "app.ratelimit")`)
- `exception` — `LubeLogException` base + specific exceptions + `GlobalExceptionHandler`

> Note: the business class for service-type *configuration on a vehicle* is `VehicleServiceService`
> (service layer for the `VehicleService` entity). The Spring `@Service` for the `Vehicle` entity
> is `VehicleService` — naming collides because of the domain. The entity for a kind of service is
> `ServiceType` (table `services`), deliberately named to avoid colliding with `@Service`.

### Auth flow

1. Keycloak handles all login (incl. Google). The backend never touches passwords or OAuth
   handshakes — it is a pure OAuth2 resource server.
2. `SecurityConfig` requires a valid JWT on every request except `/actuator/**`; stateless
   sessions, CSRF disabled.
3. After the bearer-token filter, `UserProvisioningFilter` reads the JWT subject + email and
   calls `UserService.provisionIfAbsent(...)` so a local `users` row always exists for the
   authenticated identity.
4. Controllers call `AuthUtils.getCurrentUser()` to resolve the local `User` (by `keycloak_id`)
   and pass it into the service layer, which enforces per-resource ownership.

**Test override:** `SecurityConfig`'s filter chain is gated by
`app.security.test-override.enabled` (`@ConditionalOnProperty`, default off). Integration tests
set it to `true` so `support.TestSecurityConfig` can install a mock JWT filter — no `@Profile`
needed, and production config doesn't change for tests.

### Error handling

`GlobalExceptionHandler` (`@RestControllerAdvice`) catches `LubeLogException` (the base class,
which carries an HTTP status) and returns a JSON `ErrorResponse(int status, String message)`.
New error cases should extend `LubeLogException`.

### Rate limiting

`RateLimitFilter` (a `OncePerRequestFilter`) enforces a global per-user token bucket over
`/api/**` (it skips non-`/api` paths and the public `/api/dev/**` helpers via `shouldNotFilter`).
It runs *after* `UserProvisioningFilter`, so the JWT is already in the `SecurityContextHolder`; the
bucket key is `user:<sub>` read straight from the token (no DB hit), falling back to `ip:<addr>`
when unauthenticated. `RateLimitBucketStore` holds one in-memory Bucket4j bucket per key in a
`ConcurrentHashMap` (lazy creation, no eviction — fine for a single instance; would need Redis to go
multi-instance). Limits come from `RateLimitProperties` (`app.ratelimit.{enabled,capacity,refill-tokens,refill-duration}`;
prod default 100 tokens / minute). Because the filter runs before the `DispatcherServlet`, it can't
rely on `GlobalExceptionHandler` — on rejection it writes the `429` body directly in the same
`{status, message}` shape. The filter is wired into both `SecurityConfig` and (for tests)
`support.TestSecurityConfig`, and is disabled by default in the `test` profile.

---

## Data Model

All tables in the `app` schema. UUID primary keys (`gen_random_uuid()`), `created_at TIMESTAMPTZ`.

**users** — local reference to a Keycloak identity
`id, keycloak_id (unique), email (unique), created_at`

**vehicles**
`id, user_id → users (cascade), year, make, model, trim, vin(17), nickname, mileage, created_at`

**services** (the `ServiceType` entity)
`id, user_id → users (nullable; NULL = global), name, is_global, created_at`
- Check constraint: a global service must have `user_id IS NULL`.
- Global defaults seeded in V7: Oil Change, Tire Rotation, Air Filter, Cabin Air Filter,
  Transmission Service, Brake Inspection, Coolant Flush, Spark Plugs, Differential Service,
  Fuel Filter.

**vehicle_services** (per-vehicle service configuration)
`id, vehicle_id → vehicles (cascade), service_id → services (cascade), interval_miles, interval_months, remind_when_due (default false), created_at`
- Unique `(vehicle_id, service_id)`.
- Check constraint: at least one of `interval_miles` / `interval_months` must be set.

**service_logs**
`id, vehicle_service_id → vehicle_services (cascade), done_at_mileage, done_at_date (NOT NULL), cost NUMERIC(10,2), notes, created_at`

**service_log_details** — flexible key/value pairs (no fixed columns)
`id, service_log_id → service_logs (cascade), key, value`
- Unique `(service_log_id, key)`.

**service_reminders** (per-vehicle-service reminder send state)
`id, vehicle_service_id → vehicle_services (cascade), user_id → users (cascade), sent_at, date_reminded_at, mileage_reminded_at, channel (default 'EMAIL')`
- Tracks when date- and mileage-dimension reminder emails were last sent for deduplication.

---

## API Endpoints (as implemented)

All under `/api`, all require a valid JWT. Ownership is enforced in the service layer.

**Vehicles** (`/api/vehicles`)

- `GET /api/vehicles` — list current user's vehicles
- `POST /api/vehicles` — create
- `GET /api/vehicles/{id}`
- `PATCH /api/vehicles/{id}` — partial update *(note: PATCH, not PUT)*
- `DELETE /api/vehicles/{id}`

**VIN Lookup** (`/api/vin`)

- `GET /api/vin/{vin}` — proxies NHTSA, returns decoded vehicle info (best-effort)

**Service Types** (`/api/service-types`)

- `GET /api/service-types` — globals + current user's custom types
- `POST /api/service-types` — create custom type (duplicate-guarded)
- `DELETE /api/service-types/{id}` — custom types only; deleting a global is rejected

**Vehicle Services** (`/api/vehicles/{vehicleId}/services`)

- `GET` — list configured services for the vehicle
- `POST` — activate a service type on the vehicle (set interval; optional `remindWhenDue`, defaults to false)
- `GET /{vsId}`
- `PUT /{vsId}` — update config (including `remindWhenDue`)
- `DELETE /{vsId}`

**Service Logs** (`/api/logs`) — *flat, not nested under vehicles*

- `GET /api/logs?vehicleServiceId=…` **or** `?vehicleId=…` — one of the two query params is
  required (else `400`)
- `POST /api/logs` — record a service as done (body carries `vehicleServiceId`)
- `GET /api/logs/{logId}`
- `PUT /api/logs/{logId}`
- `DELETE /api/logs/{logId}`
- `POST /api/logs/{logId}/details` — add a key/value detail
- `DELETE /api/logs/details/{detailId}` — remove a detail

---

## Key Design Decisions (don't violate)

- **Service intervals live on `vehicle_services`, not `service_types`.** The same service type can
  have different intervals on different vehicles. `service_types` holds a suggested default only.
- **`service_log_details` uses flexible key/value pairs** — no fixed columns. New service types
  never require schema changes.
- **Reminders are computed, not stored.** Next-due mileage/date are derived on read in
  `ServiceLogService` (`computeMileageDue` = done mileage + interval miles; `computeDateDue` =
  done date + interval months) and returned on `LogResponse`. There is no separate reminders
  endpoint or table yet.
- **Logging a service can advance the vehicle's odometer.** If a log's `done_at_mileage` exceeds
  the vehicle's current mileage, the vehicle mileage is updated; a log below current mileage is
  rejected (`InvalidServiceLogMileageException`).
- **VIN lookup is best-effort.** If NHTSA returns nothing, the user fills in info manually.
- **Auth is Keycloak's responsibility.** The app only validates JWTs.
- **The entity is `ServiceType` (not `Service`)** to avoid collision with `@Service`. The table is
  still `services` via `@Table(name = "services")`.
- **Single shared database** for Keycloak and the backend (chosen for simplicity — intentional
  tradeoff).

---

## Local Development

Infra (Postgres + Keycloak) runs in Docker; backend + frontend run natively. Full instructions in
**`docs/dev-setup.md`**. Summary:

```
# Terminal 1 — infra
docker compose up postgres keycloak

# Terminal 2 — backend (dev profile points datasource/Keycloak at localhost)
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 3 — frontend (Vite proxies /api → :8081, so no CORS needed in dev)
cd frontend && npm install && npm run dev
```

`docker compose up` (no args) builds and runs the full stack including the containerized backend.

---

## Testing

- **Integration tests** for all endpoints (happy paths + key error cases), using Testcontainers
  for anything touching the database: `VehicleControllerIT`, `ServiceTypeControllerIT`,
  `VehicleServiceControllerIT`, `ServiceLogControllerIT`. `RateLimitFilterIT` re-enables rate
  limiting (disabled by default in tests) with `capacity=5` and asserts the 6th request returns `429`.
- **Unit tests** for non-trivial service logic: `UserServiceTest`, `UserProvisioningFilterTest`,
  `VinClientTest`.
- `support.TestSecurityConfig` provides a mock JWT filter chain via the `test-override` property
  so tests authenticate without a real Keycloak.
</content>
</invoke>
