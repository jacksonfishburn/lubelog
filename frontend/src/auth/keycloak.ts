import Keycloak from 'keycloak-js';

export const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL ?? 'http://localhost:8080',
  realm: import.meta.env.VITE_KEYCLOAK_REALM ?? 'lubelog',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'lubelog-frontend',
});

let initPromise: Promise<boolean> | null = null;

/** Initializes Keycloak once per page load (safe under React StrictMode double-mount). */
export function initKeycloak(): Promise<boolean> {
  initPromise ??= keycloak.init({
    onLoad: 'login-required',
    pkceMethod: 'S256',
    checkLoginIframe: false,
  });
  return initPromise;
}

export function logout(): void {
  void keycloak.logout({ redirectUri: window.location.origin });
}
