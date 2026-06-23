import { useState } from 'react';
import { useServiceTypes } from '../hooks/useServiceTypes';
import { ServiceTypeList } from '../components/services/ServiceTypeList';
import { Panel } from '../components/shared/Panel';
import { Button } from '../components/shared/Button';
import { Banner } from '../components/shared/Banner';
import { LoadingBlock } from '../components/shared/Loading';
import { TextField } from '../components/shared/fields';
import { ConfirmDialog } from '../components/shared/ConfirmDialog';
import { errorMessage } from '../lib/errors';
import type { ServiceType } from '../types';

export function ServiceTypesPage() {
  const { serviceTypes, loading, error, createServiceType, deleteServiceType } = useServiceTypes();
  const [name, setName] = useState('');
  const [createError, setCreateError] = useState<string | null>(null);
  const [creating, setCreating] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<ServiceType | null>(null);
  const [deleting, setDeleting] = useState(false);

  async function handleCreate() {
    const trimmed = name.trim();
    if (!trimmed) return;
    setCreateError(null);
    setCreating(true);
    try {
      await createServiceType({ name: trimmed });
      setName('');
    } catch (e) {
      setCreateError(errorMessage(e));
    } finally {
      setCreating(false);
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await deleteServiceType(deleteTarget.id);
      setDeleteTarget(null);
    } catch (e) {
      setCreateError(errorMessage(e));
      setDeleteTarget(null);
    } finally {
      setDeleting(false);
    }
  }

  return (
    <div className="page-grid">
      <header className="page-head">
        <div>
          <h1 className="page-head__title">Service Types</h1>
          <p className="page-head__sub">
            Global defaults are seeded at startup; add custom types for your fleet
          </p>
        </div>
      </header>

      {(error || createError) && <Banner>{error ?? createError}</Banner>}

      <Panel title="Add Custom Type">
        <div className="row-wrap">
          <div className="grow" style={{ minWidth: 220 }}>
            <TextField
              label="Type Name"
              value={name}
              onChange={setName}
              placeholder="e.g. Wiper Blades"
            />
          </div>
          <Button
            variant="primary"
            onClick={() => void handleCreate()}
            loading={creating}
            disabled={!name.trim()}
            style={{ alignSelf: 'flex-end' }}
          >
            Add Type
          </Button>
        </div>
      </Panel>

      <Panel title="Type Registry" flush accent>
        {loading ? (
          <LoadingBlock label="Loading types" />
        ) : (
          <ServiceTypeList serviceTypes={serviceTypes} onDelete={setDeleteTarget} />
        )}
      </Panel>

      <ConfirmDialog
        open={deleteTarget != null}
        title="Delete Service Type"
        message={`Remove "${deleteTarget?.name}"? Vehicles already using this type keep their configuration.`}
        confirmLabel="Delete"
        danger
        loading={deleting}
        onConfirm={() => void handleDelete()}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
}
