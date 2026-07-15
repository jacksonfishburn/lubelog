import { useCallback, useState } from 'react';
import type { ServicePart } from '../types';
import * as api from '../api/ai';
import { errorMessage } from '../lib/errors';

export function useFindParts() {
  const [parts, setParts] = useState<ServicePart[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const find = useCallback(async (vehicleId: string, serviceTypeId: string) => {
    setLoading(true);
    setError(null);
    setParts(null);
    try {
      const res = await api.findParts(vehicleId, serviceTypeId);
      setParts(res.parts);
      return res.parts;
    } catch (e) {
      setError(errorMessage(e));
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const reset = useCallback(() => {
    setParts(null);
    setError(null);
    setLoading(false);
  }, []);

  return { parts, loading, error, find, reset };
}
