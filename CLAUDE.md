# LubeLog — Agent Context

## What Is LubeLog

Vehicle maintenance tracker — learning project and portfolio piece. Users log service history, configure per-vehicle service intervals, and get upcoming maintenance reminders. Supports individuals and small fleets. Backend quality is the priority. Frontend comes later.

I'm doing this project first and foremost to learn software design and how to use and integrate industry standard tooling. I am a student and want to become a backend engineer. I don't care that much about the frontend besides it being decent, but i want to understand the backend of this project really well.

**Stack:** Java + Spring Boot, PostgreSQL, Flyway, Docker + Docker Compose, Keycloak (OAuth2 / Google login), JUnit + Mockito + Testcontainers, Bucket4j (rate limiting), Springdoc (OpenAPI / Swagger UI).

---

## Where It's At

Done:

- Schema + Flyway migrations
- Keycloak + Spring Boot auth integration (Google login, JWT validation)
- Vehicle CRUD + VIN lookup (NHTSA vPIC API)
- Service types (global defaults seeded at startup + user custom types, with duplicate guard)
- Established vertical slice pattern: entity → repository → service → controller → DTOs → custom exceptions → integration tests
- Vehicle services (per-vehicle service configuration)
- Service logs + log details
- Environment-based config profiles (dev vs prod)
- Local dev setup (run Spring Boot natively against a local Postgres, bypass or mock Keycloak)

Up next (in no particular order):

- CORS + rate limiting (Bucket4j)
- Springdoc / Swagger UI
- Seeded test data
- React front end
- GitHub Actions CI/CD pipeline

**Post-MVP:**

- Email notifications for upcoming/overdue services
- AI-assisted parts lookup per service type
- Scheduled reminders sent over email

---

## How We Work

**Discuss before building.** When I bring up a feature, the default mode is to talk through the design — approach, structure, tradeoffs — before any code is written. Ask questions, flag concerns, make sure we're aligned.

**When I'm ready to build,** I'll say something like **"now build it"** or **"go ahead and implement it."** That's the signal to write actual code.

**If something is ambiguous, ask before assuming.** One focused question is better than building on a wrong assumption.

---

## How to Write Code

**Add, don't modify.** Introduce new classes and methods rather than changing things that already work. Each addition should have a clear, single responsibility. Don't spread logic into existing files unless there's a strong reason.

**Be intentional and precise.** A new service method, a new DTO, a new controller endpoint — each thing should do one thing clearly. No clever abstractions, no unnecessary generalization. If something can be its own class or method, it should be.

**Follow the established vertical slice pattern** and all other existing patterns.

**Never modify existing Flyway migrations.** New schema changes get a new migration file.

**Hibernate DDL auto is disabled.** Flyway owns the schema. JPA never generates or alters tables.

**Keep the project overview current.** `docs/project-overview.md` is the source-of-truth summary I hand to an AI for design discussions. Whenever we land a change that affects it — new endpoint, schema migration, design decision, stack/tooling addition, or status shift (something moving from "up next" to "done") — update that file in the same change and bump its "Last verified" date.

---

## Testing

Integration tests for all endpoints — happy paths and key error cases. Unit tests for service-layer logic where it's non-trivial. Testcontainers for anything that touches the database.

---

## Key Design Decisions (Don't Violate These)

- **Service intervals live on `vehicle_services`, not `service_types`.** The same service type can have different intervals on different vehicles. `service_types` holds a suggested default only.
- **`service_log_details` uses flexible key/value pairs** — no fixed columns for service-specific data. New service types never require schema changes.
- **VIN lookup is best-effort.** If NHTSA returns nothing, the user fills in manually.
- **Auth is Keycloak's responsibility.** The app only validates JWTs. It never handles passwords or OAuth handshakes.
- **The entity is `ServiceType` (not `Service`)** to avoid collision with Spring's `@Service` annotation. The table is still `services` via `@Table(name = "services")`.
- **Single shared database** for Keycloak and the backend (chosen for simplicity — intentional tradeoff).