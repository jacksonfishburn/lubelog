import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useAppState } from '../context/AppState';
import { useVehicles } from '../hooks/useVehicles';
import { useVehicleServices } from '../hooks/useVehicleServices';
import { useServiceLogs } from '../hooks/useServiceLogs';
import { useServiceTypes } from '../hooks/useServiceTypes';
import { VehicleServiceRow } from '../components/services/VehicleServiceRow';
import { VehicleServiceForm } from '../components/services/VehicleServiceForm';
import { ServiceLogForm } from '../components/services/ServiceLogForm';
import { LogEntry } from '../components/services/LogEntry';
import { VehicleForm, type VehicleFormValues } from '../components/vehicles/VehicleForm';
import { VehicleOdometer } from '../components/vehicles/VehicleOdometer';
import { Panel } from '../components/shared/Panel';
import { Button } from '../components/shared/Button';
import { Banner } from '../components/shared/Banner';
import { LoadingBlock } from '../components/shared/Loading';
import { EmptyState } from '../components/shared/EmptyState';
import { ConfirmDialog } from '../components/shared/ConfirmDialog';
import { StatusBadge } from '../components/shared/StatusBadge';
import { StatusIcon, gaugeClassForStatus } from '../components/shared/StatusIcon';
import { SERVICE_STATUS, STATUS_LABEL } from '../lib/serviceStatus';
import {
  formatVehicleName,
  formatVehicleSubtitle,
} from '../lib/format';
import type { ServiceWithStatus } from '../hooks/useFleetOverview';
import type { ServiceLog, VehicleService } from '../types';

export function CockpitPage() {
  const { vehicleId } = useParams<{ vehicleId: string }>();
  const navigate = useNavigate();
  const { setSelectedVehicleId, fleet } = useAppState();
  const { updateVehicle } = useVehicles();
  const { serviceTypes } = useServiceTypes();
  const {
    vehicleServices,
    loading: svcLoading,
    error: svcError,
    createVehicleService,
    updateVehicleService,
    deleteVehicleService,
  } = useVehicleServices(vehicleId ?? null);
  const {
    logs,
    loading: logLoading,
    error: logError,
    createLog,
    updateLog,
    deleteLog,
    deleteDetail,
  } = useServiceLogs(vehicleId ?? null);

  const [vehicleFormOpen, setVehicleFormOpen] = useState(false);
  const [svcFormOpen, setSvcFormOpen] = useState(false);
  const [svcFormMode, setSvcFormMode] = useState<'create' | 'edit'>('create');
  const [editingSvc, setEditingSvc] = useState<VehicleService | undefined>();
  const [logFormOpen, setLogFormOpen] = useState(false);
  const [logFormMode, setLogFormMode] = useState<'create' | 'edit'>('create');
  const [editingLog, setEditingLog] = useState<ServiceLog | undefined>();
  const [presetSvcId, setPresetSvcId] = useState<string | undefined>();
  const [deleteSvcTarget, setDeleteSvcTarget] = useState<VehicleService | null>(null);
  const [deleteLogTarget, setDeleteLogTarget] = useState<ServiceLog | null>(null);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    if (vehicleId) setSelectedVehicleId(vehicleId);
  }, [vehicleId, setSelectedVehicleId]);

  const overview = fleet.overviews.find((o) => o.vehicle.id === vehicleId);
  const vehicle = overview?.vehicle;

  const servicesWithStatus: ServiceWithStatus[] = useMemo(() => {
    if (overview) return overview.services;
    return vehicleServices.map((vs) => ({
      vehicleService: vs,
      logs: logs.filter((l) => l.vehicleServiceId === vs.id),
      result: {
        status: SERVICE_STATUS.NO_HISTORY,
        latestLog: null,
        milesRemaining: null,
        daysRemaining: null,
      },
    }));
  }, [overview, vehicleServices, logs]);

  const sortedLogs = useMemo(
    () => [...logs].sort((a, b) => (a.doneAtDate < b.doneAtDate ? 1 : -1)),
    [logs],
  );

  const svcNameById = useMemo(
    () => new Map(vehicleServices.map((vs) => [vs.id, vs.serviceTypeName])),
    [vehicleServices],
  );

  const maxLoggedMileage = logs.reduce(
    (max, l) => (l.doneAtMileage != null && l.doneAtMileage > max ? l.doneAtMileage : max),
    0,
  );

  if (fleet.loading && !overview) {
    return <LoadingBlock label="Loading vehicle" />;
  }

  if (!vehicleId || !vehicle) {
    return (
      <EmptyState
        icon="?"
        title="Vehicle Not Found"
        message="That vehicle isn't in the fleet registry."
        action={
          <Button variant="primary" onClick={() => navigate('/fleet')}>
            Back to Fleet
          </Button>
        }
      />
    );
  }

  function openAddService() {
    setSvcFormMode('create');
    setEditingSvc(undefined);
    setSvcFormOpen(true);
  }

  function openEditService(vs: VehicleService) {
    setSvcFormMode('edit');
    setEditingSvc(vs);
    setSvcFormOpen(true);
  }

  function openLog(svcId?: string) {
    setLogFormMode('create');
    setEditingLog(undefined);
    setPresetSvcId(svcId);
    setLogFormOpen(true);
  }

  function openEditLog(log: ServiceLog) {
    setLogFormMode('edit');
    setEditingLog(log);
    setPresetSvcId(log.vehicleServiceId);
    setLogFormOpen(true);
  }

  async function handleVehicleSave(values: VehicleFormValues) {
    if (!vehicle) return;
    await updateVehicle(vehicle.id, values);
    await fleet.refresh();
  }

  async function handleMileageUpdate(mileage: number) {
    if (!vehicle) return;
    await updateVehicle(vehicle.id, { mileage });
    await fleet.refresh();
  }

  const minMileage = Math.max(vehicle?.mileage ?? 0, maxLoggedMileage);

  const gaugeClass = overview ? gaugeClassForStatus(overview.status) : '';

  return (
    <div className="page-grid">
      <header className="page-head">
        <div>
          <div className="row" style={{ marginBottom: 'var(--sp-2)' }}>
            <Link to="/fleet" className="btn btn--ghost btn--sm">
              ← Fleet
            </Link>
          </div>
          <h1 className="page-head__title">{formatVehicleName(vehicle)}</h1>
          <p className="page-head__sub">{formatVehicleSubtitle(vehicle)}</p>
        </div>
        <div className="row">
          <Button variant="default" onClick={() => setVehicleFormOpen(true)}>
            Edit Vehicle
          </Button>
          <Button variant="primary" onClick={() => openLog()}>
            Log Service
          </Button>
        </div>
      </header>

      {(svcError || logError || fleet.error) && (
        <Banner>{svcError ?? logError ?? fleet.error}</Banner>
      )}

      <div className="page-grid page-grid--vehicle-detail">
        <div className={`vehicle-detail__status panel vehicle-detail__status-panel gauge${gaugeClass}`}>
          <div className="gauge__row">
            <div className="gauge__ring">
              {overview ? <StatusIcon status={overview.status} size="md" /> : null}
            </div>
            <div className="gauge__status grow">
              <div className="gauge__headline">
                {overview ? STATUS_LABEL[overview.status] : 'Loading status…'}
              </div>
              <div className="gauge__detail">
                {overview && (
                  <>
                    {overview.overdueCount} overdue · {overview.dueSoonCount} due soon
                  </>
                )}
              </div>
              {overview && <StatusBadge status={overview.status} />}
            </div>
            <VehicleOdometer
              mileage={vehicle.mileage}
              minMileage={minMileage}
              onUpdate={handleMileageUpdate}
            />
          </div>
        </div>

        <Panel
          className="vehicle-detail__services"
          title="Configured Services"
          flush
          actions={
            <Button size="sm" variant="primary" onClick={openAddService}>
              + Add
            </Button>
          }
        >
          {svcLoading ? (
            <LoadingBlock label="Loading services" />
          ) : servicesWithStatus.length === 0 ? (
            <EmptyState
              icon="▦"
              title="No Services"
              message="Configure maintenance intervals for this vehicle."
              action={
                <Button variant="primary" onClick={openAddService}>
                  Configure Service
                </Button>
              }
            />
          ) : (
            servicesWithStatus.map((item) => (
              <VehicleServiceRow
                key={item.vehicleService.id}
                item={item}
                onLog={() => openLog(item.vehicleService.id)}
                onEdit={() => openEditService(item.vehicleService)}
                onDelete={() => setDeleteSvcTarget(item.vehicleService)}
              />
            ))
          )}
        </Panel>

        <Panel className="vehicle-detail__history" title="Service History" flush>
          {logLoading ? (
            <LoadingBlock label="Loading logs" />
          ) : sortedLogs.length === 0 ? (
            <EmptyState
              icon="▤"
              title="No Logs Yet"
              message="Record a service to start building history."
              action={
                <Button variant="primary" onClick={() => openLog()}>
                  Log Service
                </Button>
              }
            />
          ) : (
            sortedLogs.slice(0, 8).map((log) => (
              <LogEntry
                key={log.id}
                log={log}
                serviceName={svcNameById.get(log.vehicleServiceId) ?? 'Service'}
                onEdit={() => openEditLog(log)}
                onDelete={() => setDeleteLogTarget(log)}
                onDeleteDetail={(id) => void deleteDetail(id).then(() => fleet.refresh())}
              />
            ))
          )}
          {sortedLogs.length > 8 && (
            <div style={{ padding: 'var(--sp-3) var(--sp-4)' }}>
              <Link to="/log" className="btn btn--ghost btn--sm">
                View all {sortedLogs.length} entries →
              </Link>
            </div>
          )}
        </Panel>
      </div>

      <VehicleForm
        key={`edit-${vehicle.id}-${vehicleFormOpen}`}
        open={vehicleFormOpen}
        mode="edit"
        vehicle={vehicle}
        minMileage={maxLoggedMileage || null}
        onClose={() => setVehicleFormOpen(false)}
        onSave={handleVehicleSave}
      />

      <VehicleServiceForm
        key={`${svcFormMode}-${editingSvc?.id ?? 'new'}-${svcFormOpen}`}
        open={svcFormOpen}
        mode={svcFormMode}
        vehicleService={editingSvc}
        serviceTypes={serviceTypes}
        configuredTypeIds={vehicleServices.map((vs) => vs.serviceTypeId)}
        onClose={() => setSvcFormOpen(false)}
        onSave={async (req) => {
          if (svcFormMode === 'create') await createVehicleService(req);
          else if (editingSvc) await updateVehicleService(editingSvc.id, req);
          await fleet.refresh();
        }}
      />

      <ServiceLogForm
        key={`${logFormMode}-${editingLog?.id ?? presetSvcId ?? 'new'}-${logFormOpen}`}
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

      <ConfirmDialog
        open={deleteSvcTarget != null}
        title="Remove Service"
        message={`Stop tracking "${deleteSvcTarget?.serviceTypeName}" on this vehicle?`}
        confirmLabel="Remove"
        danger
        loading={deleting}
        onConfirm={async () => {
          if (!deleteSvcTarget) return;
          setDeleting(true);
          try {
            await deleteVehicleService(deleteSvcTarget.id);
            await fleet.refresh();
            setDeleteSvcTarget(null);
          } finally {
            setDeleting(false);
          }
        }}
        onCancel={() => setDeleteSvcTarget(null)}
      />

      <ConfirmDialog
        open={deleteLogTarget != null}
        title="Delete Log"
        message="Remove this service log entry? This cannot be undone."
        confirmLabel="Delete"
        danger
        loading={deleting}
        onConfirm={async () => {
          if (!deleteLogTarget) return;
          setDeleting(true);
          try {
            await deleteLog(deleteLogTarget.id);
            await fleet.refresh();
            setDeleteLogTarget(null);
          } finally {
            setDeleting(false);
          }
        }}
        onCancel={() => setDeleteLogTarget(null)}
      />
    </div>
  );
}
