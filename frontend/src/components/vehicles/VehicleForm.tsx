import { useState } from 'react';
import { Modal } from '../shared/Modal';
import { Button } from '../shared/Button';
import { FormField, NumberField, TextField } from '../shared/fields';
import { Banner } from '../shared/Banner';
import { useVinLookup } from '../../hooks/useVinLookup';
import { errorMessage } from '../../lib/errors';
import type { Vehicle } from '../../types';

export interface VehicleFormValues {
  vin: string | null;
  nickname: string | null;
  mileage: number | null;
  year: number | null;
  make: string | null;
  model: string | null;
  trim: string | null;
}

interface VehicleFormProps {
  open: boolean;
  mode: 'create' | 'edit';
  vehicle?: Vehicle; // for edit
  minMileage?: number | null; // highest logged mileage; mileage can't drop below
  onClose: () => void;
  onSave: (values: VehicleFormValues) => Promise<void>;
}

const trimOrNull = (s: string): string | null => (s.trim() === '' ? null : s.trim());

export function VehicleForm({ open, mode, vehicle, minMileage, onClose, onSave }: VehicleFormProps) {
  const [vin, setVin] = useState(vehicle?.vin ?? '');
  const [nickname, setNickname] = useState(vehicle?.nickname ?? '');
  const [mileage, setMileage] = useState<number | null>(vehicle?.mileage ?? null);
  const [year, setYear] = useState<number | null>(vehicle?.year ?? null);
  const [make, setMake] = useState(vehicle?.make ?? '');
  const [model, setModel] = useState(vehicle?.model ?? '');
  const [trim, setTrim] = useState(vehicle?.trim ?? '');

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [vinNotice, setVinNotice] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const { lookup, loading: vinLoading } = useVinLookup();
  const vinReady = vin.trim().length === 17;

  async function handleLookup() {
    setVinNotice(null);
    const result = await lookup(vin.trim());
    if (!result) return;
    const anyData = result.year || result.make || result.model || result.trim;
    if (!anyData) {
      setVinNotice('No data returned for that VIN — fill in the details manually.');
      return;
    }
    if (result.year) setYear(result.year);
    if (result.make) setMake(result.make);
    if (result.model) setModel(result.model);
    if (result.trim) setTrim(result.trim);
    setVinNotice('Decoded — review the pre-filled fields below.');
  }

  function validate(): boolean {
    const next: Record<string, string> = {};
    if (mode === 'create' && vin.trim() !== '' && vin.trim().length !== 17) {
      next.vin = 'VIN must be exactly 17 characters';
    }
    if (mileage != null && mileage < 0) next.mileage = 'Mileage cannot be negative';
    if (mode === 'edit' && mileage != null && minMileage != null && mileage < minMileage) {
      next.mileage = `Cannot go below ${minMileage.toLocaleString()} mi (highest logged)`;
    }
    if (year != null && (year < 1900 || year > 2100)) next.year = 'Enter a year between 1900 and 2100';
    setErrors(next);
    return Object.keys(next).length === 0;
  }

  async function handleSave() {
    setSubmitError(null);
    if (!validate()) return;
    setSaving(true);
    try {
      await onSave({
        vin: trimOrNull(vin),
        nickname: trimOrNull(nickname),
        mileage,
        year,
        make: trimOrNull(make),
        model: trimOrNull(model),
        trim: trimOrNull(trim),
      });
      onClose();
    } catch (e) {
      setSubmitError(errorMessage(e));
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal
      open={open}
      title={mode === 'create' ? 'Add Vehicle' : 'Edit Vehicle'}
      onClose={onClose}
      footer={
        <>
          <Button variant="ghost" onClick={onClose} disabled={saving}>
            Cancel
          </Button>
          <Button variant="primary" onClick={handleSave} loading={saving}>
            {mode === 'create' ? 'Add Vehicle' : 'Save Changes'}
          </Button>
        </>
      }
    >
      {submitError && <Banner>{submitError}</Banner>}

      {mode === 'create' && (
        <FormField
          label="VIN"
          error={errors.vin}
          hint={
            vinNotice ??
            (vinReady ? 'Ready to decode' : 'Enter all 17 characters to enable lookup')
          }
        >
          <div className="row" style={{ gap: 'var(--sp-2)' }}>
            <input
              className={`input grow${errors.vin ? ' has-error' : ''}`}
              value={vin}
              maxLength={17}
              placeholder="1HGCM82633A004352"
              style={{ textTransform: 'uppercase' }}
              onChange={(e) => setVin(e.target.value.toUpperCase())}
            />
            <Button
              variant="default"
              onClick={handleLookup}
              disabled={!vinReady || vinLoading}
              loading={vinLoading}
            >
              {vinLoading ? 'Looking up' : 'Decode'}
            </Button>
          </div>
        </FormField>
      )}

      <TextField
        label="Nickname"
        value={nickname}
        onChange={setNickname}
        placeholder="Work Truck"
        maxLength={100}
      />

      <div className="form-grid">
        <NumberField label="Year" value={year} onChange={setYear} placeholder="2019" error={errors.year} />
        <NumberField
          label="Mileage"
          value={mileage}
          onChange={setMileage}
          placeholder="78000"
          min={0}
          error={errors.mileage}
          hint={
            mode === 'edit' && minMileage != null
              ? `Highest logged: ${minMileage.toLocaleString()} mi`
              : undefined
          }
        />
        <TextField label="Make" value={make} onChange={setMake} placeholder="Toyota" maxLength={100} />
        <TextField label="Model" value={model} onChange={setModel} placeholder="Tacoma" maxLength={100} />
        <TextField
          label="Trim"
          value={trim}
          onChange={setTrim}
          placeholder="TRD Off-Road"
          maxLength={100}
          className="span-2"
        />
      </div>
    </Modal>
  );
}
