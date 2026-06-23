import { createContext, useContext, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { useFleetOverview } from '../hooks/useFleetOverview';
import type { VehicleOverview } from '../hooks/useFleetOverview';

interface FleetState {
  overviews: VehicleOverview[];
  loading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
}

interface AppStateValue {
  selectedVehicleId: string | null;
  setSelectedVehicleId: (id: string | null) => void;
  fleet: FleetState;
}

const AppStateContext = createContext<AppStateValue | null>(null);

// Shares two things app-wide: which vehicle the cockpit is focused on, and the
// fleet-wide status overview (so the sidebar alert badge + fleet cards + cockpit
// all read one source and refresh together).
export function AppStateProvider({ children }: { children: ReactNode }) {
  const [selectedVehicleId, setSelectedVehicleId] = useState<string | null>(null);
  const { overviews, loading, error, refresh } = useFleetOverview();

  const value = useMemo<AppStateValue>(
    () => ({
      selectedVehicleId,
      setSelectedVehicleId,
      fleet: { overviews, loading, error, refresh },
    }),
    [selectedVehicleId, overviews, loading, error, refresh],
  );

  return <AppStateContext.Provider value={value}>{children}</AppStateContext.Provider>;
}

export function useAppState(): AppStateValue {
  const ctx = useContext(AppStateContext);
  if (!ctx) throw new Error('useAppState must be used within AppStateProvider');
  return ctx;
}
