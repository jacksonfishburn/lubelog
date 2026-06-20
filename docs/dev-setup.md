# Local Dev Setup

Run Postgres + Keycloak in Docker, and the backend + frontend natively on your host.

## Prereqs

- Docker (for Postgres + Keycloak)
- Java 21 (backend)
- Node (frontend)

## Run

Three terminals:

```
# Terminal 1 — infra only (Postgres + Keycloak)
docker compose up postgres keycloak

# Terminal 2 — backend (native, dev profile)
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 3 — frontend (Vite dev server on :5173)
cd frontend && npm install && npm run dev
```

The `dev` Spring profile (`backend/src/main/resources/application-dev.yml`) points the
datasource and Keycloak issuer/JWKS URIs at `localhost` instead of the Docker-network
hostnames used by the containerized backend.

The Vite dev server proxies `/api` to `http://localhost:8081`, so the frontend can call the
backend without any CORS configuration in dev.

## One-time Keycloak setup

In the admin console (`http://localhost:8080`), on the `lubelog` realm's frontend client:

- Add `http://localhost:5173/*` to **Valid redirect URIs**
- Add `http://localhost:5173/*` to **Valid post-logout redirect URIs**
- Add `http://localhost:5173` to **Web origins**

If no frontend client exists yet, create a public client (e.g. `lubelog-frontend`):
**Client authentication: Off**, **Standard flow: Enabled**, **Direct access grants: Off**
(SPA auth-code + PKCE).
