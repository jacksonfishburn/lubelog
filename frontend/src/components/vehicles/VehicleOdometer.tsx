import { useEffect, useRef, useState } from 'react';
import { Button } from '../shared/Button';
import { ConfirmDialog } from '../shared/ConfirmDialog';
import { Banner } from '../shared/Banner';
import { formatMileage } from '../../lib/format';
import { errorMessage } from '../../lib/errors';

interface VehicleOdometerProps {
  mileage: number | null;
  minMileage: number;
  onUpdate: (mileage: number) => Promise<void>;
}

export function VehicleOdometer({ mileage, minMileage, onUpdate }: VehicleOdometerProps) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(mileage != null ? String(mileage) : '');
  const [error, setError] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [pendingMileage, setPendingMileage] = useState<number | null>(null);
  const formRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    setDraft(mileage != null ? String(mileage) : '');
    setError(null);
    setSubmitError(null);
    setEditing(false);
  }, [mileage]);

  function parseDraft(): number | null {
    const trimmed = draft.trim();
    if (trimmed === '') return null;
    const n = Number(trimmed);
    return Number.isFinite(n) ? Math.round(n) : null;
  }

  function validate(value: number | null): string | null {
    if (value == null) return 'Enter the current odometer reading';
    if (value < 0) return 'Mileage cannot be negative';
    if (value < minMileage) {
      return `Cannot go below ${minMileage.toLocaleString()} mi`;
    }
    if (value === mileage) return 'That is already the current mileage';
    return null;
  }

  function cancelEdit() {
    setDraft(mileage != null ? String(mileage) : '');
    setError(null);
    setEditing(false);
  }

  function startEdit() {
    setDraft(mileage != null ? String(mileage) : '');
    setError(null);
    setEditing(true);
  }

  function handleBlur(e: React.FocusEvent) {
    const next = e.relatedTarget as Node | null;
    if (next && formRef.current?.contains(next)) return;
    if (confirmOpen || saving) return;
    cancelEdit();
  }

  function requestUpdate() {
    setSubmitError(null);
    const value = parseDraft();
    const validationError = validate(value);
    if (validationError) {
      setError(validationError);
      return;
    }
    setError(null);
    setPendingMileage(value);
    setConfirmOpen(true);
  }

  async function confirmUpdate() {
    if (pendingMileage == null) return;
    setSaving(true);
    setSubmitError(null);
    try {
      await onUpdate(pendingMileage);
      setConfirmOpen(false);
      setPendingMileage(null);
      setEditing(false);
    } catch (e) {
      setSubmitError(errorMessage(e));
      setConfirmOpen(false);
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="odometer">
      {submitError && <Banner>{submitError}</Banner>}

      {editing ? (
        <div className="odometer__form" ref={formRef} onBlur={handleBlur}>
          <span className="odometer__label">Odometer</span>
          <div className="odometer__edit-row">
            <input
              className={`input odometer__input${error ? ' has-error' : ''}`}
              type="number"
              inputMode="numeric"
              min={minMileage}
              value={draft}
              autoFocus
              onChange={(e) => {
                setDraft(e.target.value);
                setError(null);
              }}
              onKeyDown={(e) => {
                if (e.key === 'Enter') requestUpdate();
                if (e.key === 'Escape') cancelEdit();
              }}
            />
            <Button variant="primary" size="sm" onMouseDown={(e) => e.preventDefault()} onClick={requestUpdate}>
              Update
            </Button>
          </div>
          {error && <span className="field__error">⚠ {error}</span>}
        </div>
      ) : (
        <div className="odometer__display">
          <span className="odometer__label">Odometer</span>
          <span className="odometer__current">{formatMileage(mileage)}</span>
          <Button variant="default" size="sm" onClick={startEdit}>
            Update
          </Button>
        </div>
      )}

      <ConfirmDialog
        open={confirmOpen}
        title="Update Odometer?"
        message={`Set mileage to ${pendingMileage?.toLocaleString() ?? '—'} mi? Make sure this is correct — you cannot change it to a lower value later.`}
        confirmLabel="Yes, Update"
        loading={saving}
        onConfirm={() => void confirmUpdate()}
        onCancel={() => {
          if (!saving) {
            setConfirmOpen(false);
            setPendingMileage(null);
          }
        }}
      />
    </div>
  );
}
