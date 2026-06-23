import { useKeycloak } from '../auth/KeycloakProvider';
import { userFromKeycloak } from '../auth/user';

// Signed-in identity from the Keycloak JWT (sub + email). No backend call.
export function useCurrentUser() {
  const keycloak = useKeycloak();
  const user = userFromKeycloak(keycloak);

  return {
    user,
    loading: false,
    error: user ? null : 'Not authenticated',
  };
}
