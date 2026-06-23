import { useCallback, useEffect, useState } from 'react';
import type { CreateVehicleRequest, UpdateVehicleRequest, Vehicle } from '../types';
import * as api from '../mock/vehicles';
import { errorMessage } from '../lib/errors';

// Owns the vehicle collection + CRUD. Components never touch the mock layer
// directly; only this hook's internals change when the real API arrives.
export function useVehicles() {
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setVehicles(await api.listVehicles());
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const createVehicle = useCallback(async (req: CreateVehicleRequest) => {
    const created = await api.createVehicle(req);
    setVehicles((prev) => [...prev, created]);
    return created;
  }, []);

  const updateVehicle = useCallback(async (id: string, req: UpdateVehicleRequest) => {
    const updated = await api.updateVehicle(id, req);
    setVehicles((prev) => prev.map((v) => (v.id === id ? updated : v)));
    return updated;
  }, []);

  const deleteVehicle = useCallback(async (id: string) => {
    await api.deleteVehicle(id);
    setVehicles((prev) => prev.filter((v) => v.id !== id));
  }, []);

  return { vehicles, loading, error, refresh, createVehicle, updateVehicle, deleteVehicle };
}
