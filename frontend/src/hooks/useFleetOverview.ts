import { useCallback, useEffect, useState } from 'react';
import type { ServiceLog, Vehicle, VehicleService } from '../types';
import type { ServiceStatus, ServiceStatusResult } from '../lib/serviceStatus';
import { SERVICE_STATUS, computeServiceStatus, worstStatus } from '../lib/serviceStatus';
import { listVehicles } from '../api/vehicles';
import { listVehicleServices } from '../api/vehicleservices';
import { listLogsByVehicle } from '../api/servicelogs';
import { errorMessage } from '../lib/errors';

export interface ServiceWithStatus {
  vehicleService: VehicleService;
  logs: ServiceLog[];
  result: ServiceStatusResult;
}

export interface VehicleOverview {
  vehicle: Vehicle;
  services: ServiceWithStatus[];
  status: ServiceStatus; // worst across configured services
  overdueCount: number;
  dueSoonCount: number;
}

// Composes the per-resource endpoints into a fleet-wide, status-annotated view.
// This is read-only aggregate state; mutations go through the resource hooks
// and then call refresh() here so every status indicator stays in sync.
export function useFleetOverview() {
  const [overviews, setOverviews] = useState<VehicleOverview[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const vehicles = await listVehicles();
      const result = await Promise.all(
        vehicles.map(async (vehicle): Promise<VehicleOverview> => {
          const [services, logs] = await Promise.all([
            listVehicleServices(vehicle.id),
            listLogsByVehicle(vehicle.id),
          ]);
          const withStatus: ServiceWithStatus[] = services.map((vs) => {
            const vsLogs = logs.filter((l) => l.vehicleServiceId === vs.id);
            return { vehicleService: vs, logs: vsLogs, result: computeServiceStatus(vsLogs, vehicle.mileage) };
          });
          const statuses = withStatus.map((s) => s.result.status);
          return {
            vehicle,
            services: withStatus,
            status: worstStatus(statuses),
            overdueCount: statuses.filter((s) => s === SERVICE_STATUS.OVERDUE).length,
            dueSoonCount: statuses.filter((s) => s === SERVICE_STATUS.DUE_SOON).length,
          };
        }),
      );
      setOverviews(result);
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  return { overviews, loading, error, refresh };
}
