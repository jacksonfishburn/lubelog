// Mirrors the `users` row exposed by the backend (see project-overview Data Model).
// There is no UserResponse DTO yet; this is the minimal identity the UI needs.
export interface User {
  id: string;
  keycloakId: string;
  email: string;
  createdAt: string; // ISO-8601 instant
}
