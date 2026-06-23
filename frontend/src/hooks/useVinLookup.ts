import { useCallback, useState } from 'react';
import type { VinDecodeResult } from '../types';
import * as api from '../api/vehicles';
import { errorMessage } from '../lib/errors';

// Simulates the NHTSA round-trip with a dedicated loading state, so the VIN
// field can show "Looking up…" before pre-filling year/make/model/trim.
export function useVinLookup() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const lookup = useCallback(async (vin: string): Promise<VinDecodeResult | null> => {
    setLoading(true);
    setError(null);
    try {
      return await api.decodeVin(vin);
    } catch (e) {
      setError(errorMessage(e));
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  return { lookup, loading, error };
}
