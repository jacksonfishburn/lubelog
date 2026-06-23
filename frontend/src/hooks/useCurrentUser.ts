import { useEffect, useState } from 'react';
import type { User } from '../types';
import { getCurrentUser } from '../mock/user';
import { errorMessage } from '../lib/errors';

// Resolves the (mock) signed-in user. Swap the import to the real identity
// source later; the component contract here stays the same.
export function useCurrentUser() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    getCurrentUser()
      .then((u) => active && setUser(u))
      .catch((e) => active && setError(errorMessage(e)))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, []);

  return { user, loading, error };
}
