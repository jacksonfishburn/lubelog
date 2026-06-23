import { useState } from 'react';
import type { CreateLogRequest, LogDetailRequest, ServiceLog, VehicleService } from '../../types';
import { Modal } from '../shared/Modal';
import { Button } from '../shared/Button';
import { Banner } from '../shared/Banner';
import { NumberField, SelectField, TextAreaField, TextField } from '../shared/fields';
import { errorMessage } from '../../lib/errors';

interface ServiceLogFormProps {
  open: boolean;
  mode: 'create' | 'edit';
  log?: ServiceLog;
  vehicleServices: VehicleService[];
  presetVehicleServiceId?: string;
  currentMileage?: number | null;
  onClose: () => void;
  onSave: (req: CreateLogRequest) => Promise<void>;
}

function todayIso(): string {
  const d = new Date();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mm}-${dd}`;
}

export function ServiceLogForm({
  open,
  mode,
  log,
  vehicleServices,
  presetVehicleServiceId,
  currentMileage,
  onClose,
  onSave,
}: ServiceLogFormProps) {
  const [vehicleServiceId, setVehicleServiceId] = useState(
    log?.vehicleServiceId ?? presetVehicleServiceId ?? '',
  );
  const [doneAtDate, setDoneAtDate] = useState(log?.doneAtDate ?? todayIso());
  const [doneAtMileage, setDoneAtMileage] = useState<number | null>(log?.doneAtMileage ?? currentMileage ?? null);
  const [cost, setCost] = useState<number | null>(log?.cost ?? null);
  const [notes, setNotes] = useState(log?.notes ?? '');
  const [details, setDetails] = useState<LogDetailRequest[]>(
    log?.details.map((d) => ({ key: d.key, value: d.value })) ?? [],
  );
  const [detailKey, setDetailKey] = useState('');
  const [detailValue, setDetailValue] = useState('');

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const serviceLocked = mode === 'edit' || Boolean(presetVehicleServiceId);

  function addDetail() {
    const k = detailKey.trim();
    const v = detailValue.trim();
    if (!k || !v) return;
    setDetails((prev) => [...prev, { key: k, value: v }]);
    setDetailKey('');
    setDetailValue('');
  }

  function validate(): boolean {
    const next: Record<string, string> = {};
    if (!vehicleServiceId) next.vehicleServiceId = 'Pick a service';
    if (!doneAtDate.trim()) next.doneAtDate = 'Date is required';
    if (doneAtMileage != null && doneAtMileage < 0) next.doneAtMileage = 'Mileage cannot be negative';
    setErrors(next);
    return Object.keys(next).length === 0;
  }

  async function handleSave() {
    setSubmitError(null);
    if (!validate()) return;
    setSaving(true);
    try {
      await onSave({
        vehicleServiceId,
        doneAtDate: doneAtDate.trim(),
        doneAtMileage,
        cost,
        notes: notes.trim() === '' ? null : notes.trim(),
        details: details.length > 0 ? details : undefined,
      });
      onClose();
    } catch (e) {
      setSubmitError(errorMessage(e));
    } finally {
      setSaving(false);
    }
  }

  const options = vehicleServices.map((vs) => ({
    value: vs.id,
    label: vs.serviceTypeName,
  }));

  return (
    <Modal
      open={open}
      title={mode === 'create' ? 'Log Service' : 'Edit Log Entry'}
      wide
      onClose={onClose}
      footer={
        <>
          <Button variant="ghost" onClick={onClose} disabled={saving}>
            Cancel
          </Button>
          <Button variant="primary" onClick={handleSave} loading={saving}>
            {mode === 'create' ? 'Record Service' : 'Save Log'}
          </Button>
        </>
      }
    >
      {submitError && <Banner>{submitError}</Banner>}

      {serviceLocked ? (
        <SelectField
          label="Service"
          value={vehicleServiceId}
          onChange={() => {}}
          options={options.filter((o) => o.value === vehicleServiceId)}
          required
          hint="Service is fixed for this entry."
        />
      ) : (
        <SelectField
          label="Service"
          value={vehicleServiceId}
          onChange={setVehicleServiceId}
          options={options}
          placeholder="Select configured service…"
          required
          error={errors.vehicleServiceId}
        />
      )}

      <div className="form-grid">
        <TextField
          label="Date Performed"
          value={doneAtDate}
          onChange={setDoneAtDate}
          placeholder="YYYY-MM-DD"
          required
          error={errors.doneAtDate}
        />
        <NumberField
          label="Odometer"
          value={doneAtMileage}
          onChange={setDoneAtMileage}
          placeholder={currentMileage != null ? String(currentMileage) : '78000'}
          min={0}
          error={errors.doneAtMileage}
          hint={currentMileage != null ? `Current: ${currentMileage.toLocaleString()} mi` : undefined}
        />
        <NumberField
          label="Cost"
          value={cost}
          onChange={setCost}
          placeholder="0.00"
          min={0}
          className="span-2"
        />
      </div>

      <TextAreaField label="Notes" value={notes} onChange={setNotes} placeholder="Optional notes…" />

      <div className="stack-sm">
        <span className="field__label">Details (key / value)</span>
        {details.length > 0 && (
          <div className="kv-list">
            {details.map((d, i) => (
              <span className="kv" key={`${d.key}-${i}`}>
                <span className="kv__k">{d.key}</span>
                <span className="kv__v">{d.value}</span>
                <button
                  type="button"
                  className="kv__del"
                  onClick={() => setDetails((prev) => prev.filter((_, j) => j !== i))}
                >
                  ×
                </button>
              </span>
            ))}
          </div>
        )}
        <div className="detail-adder">
          <TextField label="" value={detailKey} onChange={setDetailKey} placeholder="Key" />
          <TextField label="" value={detailValue} onChange={setDetailValue} placeholder="Value" />
          <Button variant="default" onClick={addDetail} disabled={!detailKey.trim() || !detailValue.trim()}>
            Add
          </Button>
        </div>
      </div>
    </Modal>
  );
}
