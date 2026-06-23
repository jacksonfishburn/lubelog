import { useCallback, useEffect, useState } from 'react';
import type { CreateServiceTypeRequest, ServiceType } from '../types';
import * as api from '../mock/servicetypes';
import { errorMessage } from '../lib/errors';

export function useServiceTypes() {
  const [serviceTypes, setServiceTypes] = useState<ServiceType[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setServiceTypes(await api.listServiceTypes());
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const createServiceType = useCallback(async (req: CreateServiceTypeRequest) => {
    const created = await api.createServiceType(req);
    await refresh(); // re-sort (globals first, then alphabetical)
    return created;
  }, [refresh]);

  const deleteServiceType = useCallback(async (id: string) => {
    await api.deleteServiceType(id);
    setServiceTypes((prev) => prev.filter((t) => t.id !== id));
  }, []);

  return { serviceTypes, loading, error, refresh, createServiceType, deleteServiceType };
}
