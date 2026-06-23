// Identity shown in the UI. Sourced from the Keycloak JWT (sub + email).
// Local `users.id` / `createdAt` are backend-only until a GET /api/me exists.
export interface User {
  keycloakId: string;
  email: string;
}
