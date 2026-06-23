import { keycloak } from '../auth/keycloak';
import { ApiError } from '../lib/errors';

const API_BASE = '/api';

interface ApiErrorBody {
  message?: string;
  status?: number;
}

async function ensureFreshToken(): Promise<void> {
  await keycloak.updateToken(30);
  if (!keycloak.token) {
    throw new Error('Not authenticated');
  }
}

function resolveUrl(path: string): string {
  if (path.startsWith('/api')) return path;
  const normalized = path.startsWith('/') ? path : `/${path}`;
  return `${API_BASE}${normalized}`;
}

export async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {
  await ensureFreshToken();

  const headers = new Headers(options.headers);
  headers.set('Authorization', `Bearer ${keycloak.token}`);
  if (options.body != null && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  const res = await fetch(resolveUrl(path), { ...options, headers });

  if (!res.ok) {
    let message = res.statusText;
    try {
      const body = (await res.json()) as ApiErrorBody;
      if (body.message) message = body.message;
    } catch {
      // ignore non-JSON error bodies
    }
    throw new ApiError(message, res.status);
  }

  if (res.status === 204) {
    return undefined as T;
  }

  return res.json() as Promise<T>;
}
