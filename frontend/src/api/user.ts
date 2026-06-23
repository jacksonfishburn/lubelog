import { keycloak } from '../auth/keycloak';
import type { User } from '../types';

// No GET /api/me yet — identity comes from the Keycloak access token.
// `id` and `createdAt` are placeholders until a backend profile endpoint exists.
export async function getCurrentUser(): Promise<User> {
  await keycloak.updateToken(30);
  const parsed = keycloak.tokenParsed;
  if (!parsed?.sub) {
    throw new Error('Not authenticated');
  }

  return {
    id: parsed.sub,
    keycloakId: parsed.sub,
    email: typeof parsed.email === 'string' ? parsed.email : '',
    createdAt: new Date(0).toISOString(),
  };
}
