import { useCallback, useEffect, useState } from 'react';
import type { VehicleService, VehicleServiceRequest } from '../types';
import * as api from '../api/vehicleservices';
import { errorMessage } from '../lib/errors';

// Per-vehicle service configuration. Pass null when no vehicle is selected;
// the hook stays idle and returns an empty list.
export function useVehicleServices(vehicleId: string | null) {
  const [vehicleServices, setVehicleServices] = useState<VehicleService[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    if (!vehicleId) {
      setVehicleServices([]);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      setVehicleServices(await api.listVehicleServices(vehicleId));
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }, [vehicleId]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const createVehicleService = useCallback(
    async (req: VehicleServiceRequest) => {
      if (!vehicleId) throw new Error('No vehicle selected');
      const created = await api.createVehicleService(vehicleId, req);
      setVehicleServices((prev) => [...prev, created]);
      return created;
    },
    [vehicleId],
  );

  const updateVehicleService = useCallback(
    async (vsId: string, req: VehicleServiceRequest) => {
      if (!vehicleId) throw new Error('No vehicle selected');
      const updated = await api.updateVehicleService(vehicleId, vsId, req);
      setVehicleServices((prev) => prev.map((vs) => (vs.id === vsId ? updated : vs)));
      return updated;
    },
    [vehicleId],
  );

  const deleteVehicleService = useCallback(
    async (vsId: string) => {
      if (!vehicleId) throw new Error('No vehicle selected');
      await api.deleteVehicleService(vehicleId, vsId);
      setVehicleServices((prev) => prev.filter((vs) => vs.id !== vsId));
    },
    [vehicleId],
  );

  return {
    vehicleServices,
    loading,
    error,
    refresh,
    createVehicleService,
    updateVehicleService,
    deleteVehicleService,
  };
}
