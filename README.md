# LubeLog

[lubelog.jacksonfishburn.dev](https://lubelog.jacksonfishburn.dev)

A vehicle maintenance tracker with scheduled email reminders and AI Part Finding feature. 

I came to this idea after finishing Cabinet and trying to think of a project that would require a larger schema, more complicated API, and greater potential for scaling. I worked at a JiffyLube for most of my time in high school, which was a great background for understanding what this tool should look like, allowing for my whole focus to be on designing and building it. 
## What I Learned

- Managing a larger system — bigger schema, more endpoints and services, more third-party integrations and dependencies
- Establishing a vertical slice pattern for feature separation:
   - entity → repository → service → controller → DTOs → custom exceptions → integration tests
- Designing a more complicated schema
- Using Lombok to simplify code
- Learning about and using database migrations
- Calling a client from within the service
- Integrating a CI/CD pipeline for easy deployment
- Utilizing a 3rd party for auth management
- Using AI effectively for code generation (when and when not to use, writing effective prompts, managing context)
- Integrating AI features into the application

## Tech Stack 

| Layer            | Technology                                                                         |
| ---------------- | ---------------------------------------------------------------------------------- |
| Backend API      | Java + Spring Boot                                                                 |
| Database         | PostgreSQL                                                                         |
| Migrations       | Flyway                                                                             |
| Auth             | Keycloak (OAuth2 / Google login); backend is a resource server validating JWTs     |
| VIN Lookup       | NHTSA vPIC API                                                                     |
| Containerization | Docker + Docker Compose                                                            |
| Testing          | JUnit + Mockito + Testcontainers (Postgres) + spring-security-test                 |
| Monitoring       | Spring Boot Actuator                                                               |
| Rate limiting    | Bucket4j - in-memory, per-user rate limiting                                       |
| Frontend         | React 19 + Vite + TypeScript; Keycloak-js (PKCE) + authenticated `apiFetch` client |
| CI/CD            | GitHub Actions                                                                     |

### Features

- Schema + Flyway migrations
- Keycloak + Spring Boot auth integration (Google login, JWT validation)
- Vehicle CRUD + VIN lookup (NHTSA vPIC API)
- Service types (global defaults seeded at startup + user custom types, with duplicate guard)
- Vehicle services (per-vehicle service configuration)
- Service logs + log details
- Environment-based config profiles (dev vs prod)
- Local dev setup (run Spring Boot natively against a local Postgres, bypass or mock Keycloak)
- Springdoc / Swagger UI
- Rate limiting (Bucket4j)
- GitHub Actions CI/CD pipeline
- Email notifications for upcoming/overdue services
- AI-assisted parts lookup per service type

