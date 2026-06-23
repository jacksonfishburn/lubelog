import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import type Keycloak from 'keycloak-js';
import { LoadingBlock } from '../components/shared/Loading';
import { keycloak, initKeycloak } from './keycloak';

const KeycloakContext = createContext<Keycloak | null>(null);

export function useKeycloak(): Keycloak {
  const ctx = useContext(KeycloakContext);
  if (!ctx) {
    throw new Error('useKeycloak must be used within KeycloakProvider');
  }
  return ctx;
}

interface KeycloakProviderProps {
  children: ReactNode;
}

export function KeycloakProvider({ children }: KeycloakProviderProps) {
  const [ready, setReady] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;

    initKeycloak()
      .then((authenticated) => {
        if (!active) return;
        if (!authenticated) {
          void keycloak.login();
          return;
        }
        setReady(true);
      })
      .catch((e: unknown) => {
        if (!active) return;
        setError(e instanceof Error ? e.message : 'Authentication failed');
      });

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    if (!ready) return;

    const interval = window.setInterval(() => {
      keycloak.updateToken(70).catch(() => {
        void keycloak.login();
      });
    }, 60_000);

    return () => clearInterval(interval);
  }, [ready]);

  if (error) {
    return (
      <div className="loading-block" style={{ margin: '2rem' }}>
        Sign-in failed: {error}
      </div>
    );
  }

  if (!ready) {
    return <LoadingBlock label="Signing in" />;
  }

  return <KeycloakContext.Provider value={keycloak}>{children}</KeycloakContext.Provider>;
}
