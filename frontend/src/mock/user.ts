// ── MOCK DATA LAYER ──────────────────────────────────────────────────────────
// Stands in for the authenticated user. Real auth is Keycloak; the backend
// resolves the current user from the JWT. Here we hardcode one identity.
//
// TO GO REAL: there is no GET /api/me endpoint yet — the current user is implied
// by the bearer token. Replace getCurrentUser() with whatever resolves identity
// (decode the token, or call a future /api/me). Base URL: requests proxy to
// http://localhost:8081 via Vite (see vite.config.ts).
import type { User } from '../types';
import { delay } from '../lib/latency';

// Shared owner id — every mock vehicle belongs to this user.
export const MOCK_USER_ID = '11111111-1111-1111-1111-111111111111';

export const mockUser: User = {
  keycloakId: 'kc-sub-mock-0001',
  email: 'jacksonfishburn@gmail.com',
};

export async function getCurrentUser(): Promise<User> {
  return delay({ ...mockUser });
}
