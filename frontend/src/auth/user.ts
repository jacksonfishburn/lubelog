import type Keycloak from 'keycloak-js';
import type { User } from '../types';

/** Builds UI user identity from Keycloak token claims (no /api/me round trip). */
export function userFromKeycloak(keycloak: Keycloak): User | null {
  const parsed = keycloak.tokenParsed;
  if (!parsed?.sub) return null;

  return {
    keycloakId: parsed.sub,
    email: typeof parsed.email === 'string' ? parsed.email : '',
  };
}
