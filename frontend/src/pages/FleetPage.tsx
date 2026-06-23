import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppState } from '../context/AppState';
import { useVehicles } from '../hooks/useVehicles';
import { VehicleCard } from '../components/vehicles/VehicleCard';
import { VehicleForm, type VehicleFormValues } from '../components/vehicles/VehicleForm';
import { ConfirmDialog } from '../components/shared/ConfirmDialog';
import { Button } from '../components/shared/Button';
import { Banner } from '../components/shared/Banner';
import { LoadingBlock } from '../components/shared/Loading';
import { EmptyState } from '../components/shared/EmptyState';
import type { Vehicle } from '../types';

export function FleetPage() {
  const navigate = useNavigate();
  const { selectedVehicleId, setSelectedVehicleId, fleet } = useAppState();
  const { createVehicle, updateVehicle, deleteVehicle } = useVehicles();

  const [formOpen, setFormOpen] = useState(false);
  const [formMode, setFormMode] = useState<'create' | 'edit'>('create');
  const [editing, setEditing] = useState<Vehicle | undefined>();
  const [deleteTarget, setDeleteTarget] = useState<Vehicle | null>(null);
  const [deleting, setDeleting] = useState(false);

  const { overviews, loading, error } = fleet;

  function openCreate() {
    setFormMode('create');
    setEditing(undefined);
    setFormOpen(true);
  }

  function openEdit(vehicle: Vehicle) {
    setFormMode('edit');
    setEditing(vehicle);
    setFormOpen(true);
  }

  function openVehicle(vehicleId: string) {
    setSelectedVehicleId(vehicleId);
    navigate(`/fleet/${vehicleId}`);
  }

  async function handleSave(values: VehicleFormValues) {
    if (formMode === 'create') {
      await createVehicle(values);
    } else if (editing) {
      await updateVehicle(editing.id, values);
    }
    await fleet.refresh();
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await deleteVehicle(deleteTarget.id);
      if (selectedVehicleId === deleteTarget.id) setSelectedVehicleId(null);
      await fleet.refresh();
      setDeleteTarget(null);
    } finally {
      setDeleting(false);
    }
  }

  return (
    <div className="page-grid">
      <header className="page-head">
        <div>
          <h1 className="page-head__title">Fleet Registry</h1>
          <p className="page-head__sub">Manage vehicles and view maintenance details</p>
        </div>
        <Button variant="primary" onClick={openCreate}>
          + Add Vehicle
        </Button>
      </header>

      {error && <Banner>{error}</Banner>}

      {loading ? (
        <LoadingBlock label="Loading fleet" />
      ) : overviews.length === 0 ? (
        <EmptyState
          icon="⊞"
          title="No Vehicles"
          message="Add your first vehicle to start tracking maintenance intervals."
          action={
            <Button variant="primary" onClick={openCreate}>
              Add Vehicle
            </Button>
          }
        />
      ) : (
        <div className="vehicle-grid">
          {overviews.map((overview) => (
            <VehicleCard
              key={overview.vehicle.id}
              overview={overview}
              selected={selectedVehicleId === overview.vehicle.id}
              onOpen={() => openVehicle(overview.vehicle.id)}
              onEdit={() => openEdit(overview.vehicle)}
              onDelete={() => setDeleteTarget(overview.vehicle)}
            />
          ))}
        </div>
      )}

      <VehicleForm
        key={`${formMode}-${editing?.id ?? 'new'}-${formOpen}`}
        open={formOpen}
        mode={formMode}
        vehicle={editing}
        onClose={() => setFormOpen(false)}
        onSave={handleSave}
      />

      <ConfirmDialog
        open={deleteTarget != null}
        title="Delete Vehicle"
        message={`Remove "${deleteTarget?.nickname ?? 'this vehicle'}" from the fleet? Configured services and logs will be removed.`}
        confirmLabel="Delete"
        danger
        loading={deleting}
        onConfirm={() => void handleDelete()}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
}
