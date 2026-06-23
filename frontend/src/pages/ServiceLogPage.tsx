import { useMemo, useState } from 'react';
import { useAppState } from '../context/AppState';
import { useServiceLogs } from '../hooks/useServiceLogs';
import { useVehicleServices } from '../hooks/useVehicleServices';
import { LogEntry } from '../components/services/LogEntry';
import { ServiceLogForm } from '../components/services/ServiceLogForm';
import { Panel } from '../components/shared/Panel';
import { Button } from '../components/shared/Button';
import { Banner } from '../components/shared/Banner';
import { LoadingBlock } from '../components/shared/Loading';
import { EmptyState } from '../components/shared/EmptyState';
import { ConfirmDialog } from '../components/shared/ConfirmDialog';
import { formatVehicleName } from '../lib/format';
import type { ServiceLog } from '../types';

export function ServiceLogPage() {
  const { selectedVehicleId, setSelectedVehicleId, fleet } = useAppState();
  const activeId = selectedVehicleId ?? fleet.overviews[0]?.vehicle.id ?? null;
  const activeOverview = fleet.overviews.find((o) => o.vehicle.id === activeId);
  const vehicle = activeOverview?.vehicle;

  const { vehicleServices } = useVehicleServices(activeId);
  const { logs, loading, error, createLog, updateLog, deleteLog, deleteDetail } =
    useServiceLogs(activeId);

  const [logFormOpen, setLogFormOpen] = useState(false);
  const [logFormMode, setLogFormMode] = useState<'create' | 'edit'>('create');
  const [editingLog, setEditingLog] = useState<ServiceLog | undefined>();
  const [presetSvcId, setPresetSvcId] = useState<string | undefined>();
  const [deleteTarget, setDeleteTarget] = useState<ServiceLog | null>(null);
  const [deleting, setDeleting] = useState(false);

  const svcNameById = useMemo(
    () => new Map(vehicleServices.map((vs) => [vs.id, vs.serviceTypeName])),
    [vehicleServices],
  );

  const sortedLogs = useMemo(
    () => [...logs].sort((a, b) => (a.doneAtDate < b.doneAtDate ? 1 : -1)),
    [logs],
  );

  function openCreate() {
    setLogFormMode('create');
    setEditingLog(undefined);
    setPresetSvcId(undefined);
    setLogFormOpen(true);
  }

  function openEdit(log: ServiceLog) {
    setLogFormMode('edit');
    setEditingLog(log);
    setPresetSvcId(log.vehicleServiceId);
    setLogFormOpen(true);
  }

  return (
    <div className="page-grid">
      <header className="page-head">
        <div>
          <h1 className="page-head__title">Service Log</h1>
          <p className="page-head__sub">Complete maintenance history by vehicle</p>
        </div>
        {vehicle && vehicleServices.length > 0 && (
          <Button variant="primary" onClick={openCreate}>
            + Log Service
          </Button>
        )}
      </header>

      {fleet.overviews.length > 0 && (
        <div className="vsel">
          {fleet.overviews.map((o) => (
            <button
              key={o.vehicle.id}
              type="button"
              className={`vsel__btn${activeId === o.vehicle.id ? ' is-active' : ''}`}
              onClick={() => setSelectedVehicleId(o.vehicle.id)}
            >
              {formatVehicleName(o.vehicle)}
            </button>
          ))}
        </div>
      )}

      {(error || fleet.error) && <Banner>{error ?? fleet.error}</Banner>}

      {fleet.loading ? (
        <LoadingBlock label="Loading fleet" />
      ) : !vehicle ? (
        <EmptyState
          icon="▤"
          title="No Vehicles"
          message="Add a vehicle to start logging maintenance."
        />
      ) : (
        <Panel title={`${formatVehicleName(vehicle)} — Log Archive`} flush>
          {loading ? (
            <LoadingBlock label="Loading logs" />
          ) : sortedLogs.length === 0 ? (
            <EmptyState
              icon="▤"
              title="No Entries"
              message={
                vehicleServices.length === 0
                  ? 'Configure services on this vehicle first, then log maintenance.'
                  : 'Record your first service for this vehicle.'
              }
              action={
                vehicleServices.length > 0 ? (
                  <Button variant="primary" onClick={openCreate}>
                    Log Service
                  </Button>
                ) : undefined
              }
            />
          ) : (
            sortedLogs.map((log) => (
              <LogEntry
                key={log.id}
                log={log}
                serviceName={svcNameById.get(log.vehicleServiceId) ?? 'Service'}
                onEdit={() => openEdit(log)}
                onDelete={() => setDeleteTarget(log)}
                onDeleteDetail={(id) => void deleteDetail(id).then(() => fleet.refresh())}
              />
            ))
          )}
        </Panel>
      )}

      {vehicle && (
        <ServiceLogForm
          key={`${logFormMode}-${editingLog?.id ?? 'new'}-${logFormOpen}`}
          open={logFormOpen}
          mode={logFormMode}
          log={editingLog}
          vehicleServices={vehicleServices}
          presetVehicleServiceId={presetSvcId}
          currentMileage={vehicle.mileage}
          onClose={() => setLogFormOpen(false)}
          onSave={async (req) => {
            if (logFormMode === 'create') await createLog(req);
            else if (editingLog) await updateLog(editingLog.id, req);
            await fleet.refresh();
          }}
        />
      )}

      <ConfirmDialog
        open={deleteTarget != null}
        title="Delete Log"
        message="Remove this service log entry?"
        confirmLabel="Delete"
        danger
        loading={deleting}
        onConfirm={async () => {
          if (!deleteTarget) return;
          setDeleting(true);
          try {
            await deleteLog(deleteTarget.id);
            await fleet.refresh();
            setDeleteTarget(null);
          } finally {
            setDeleting(false);
          }
        }}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
}
