import { useCallback, useEffect, useState } from 'react';
import type { CreateLogRequest, LogDetailRequest, ServiceLog } from '../types';
import * as api from '../api/servicelogs';
import { errorMessage } from '../lib/errors';

// All logs for a vehicle (across its configured services). Mutations re-fetch
// because logging can ripple into computed due values and odometer state.
export function useServiceLogs(vehicleId: string | null) {
  const [logs, setLogs] = useState<ServiceLog[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    if (!vehicleId) {
      setLogs([]);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      setLogs(await api.listLogsByVehicle(vehicleId));
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }, [vehicleId]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const createLog = useCallback(
    async (req: CreateLogRequest) => {
      const created = await api.createLog(req);
      await refresh();
      return created;
    },
    [refresh],
  );

  const updateLog = useCallback(
    async (logId: string, req: CreateLogRequest) => {
      const updated = await api.updateLog(logId, req);
      await refresh();
      return updated;
    },
    [refresh],
  );

  const deleteLog = useCallback(
    async (logId: string) => {
      await api.deleteLog(logId);
      setLogs((prev) => prev.filter((l) => l.id !== logId));
    },
    [],
  );

  const addDetail = useCallback(
    async (logId: string, req: LogDetailRequest) => {
      const created = await api.addDetail(logId, req);
      await refresh();
      return created;
    },
    [refresh],
  );

  const deleteDetail = useCallback(
    async (detailId: string) => {
      await api.deleteDetail(detailId);
      await refresh();
    },
    [refresh],
  );

  return { logs, loading, error, refresh, createLog, updateLog, deleteLog, addDetail, deleteDetail };
}
