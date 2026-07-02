import { useState } from 'react';
import { Modal } from '../shared/Modal';
import { Button } from '../shared/Button';
import { CheckboxField, NumberField, SelectField } from '../shared/fields';
import { Banner } from '../shared/Banner';
import { errorMessage } from '../../lib/errors';
import type { ServiceType, VehicleService, VehicleServiceRequest } from '../../types';

interface VehicleServiceFormProps {
  open: boolean;
  mode: 'create' | 'edit';
  vehicleService?: VehicleService;
  serviceTypes: ServiceType[];
  configuredTypeIds: string[]; // already on this vehicle — disabled on create
  onClose: () => void;
  onSave: (req: VehicleServiceRequest) => Promise<void>;
}

export function VehicleServiceForm({
  open,
  mode,
  vehicleService,
  serviceTypes,
  configuredTypeIds,
  onClose,
  onSave,
}: VehicleServiceFormProps) {
  const [serviceTypeId, setServiceTypeId] = useState(vehicleService?.serviceTypeId ?? '');
  const [intervalMiles, setIntervalMiles] = useState<number | null>(vehicleService?.intervalMiles ?? null);
  const [intervalMonths, setIntervalMonths] = useState<number | null>(vehicleService?.intervalMonths ?? null);
  const [remindWhenDue, setRemindWhenDue] = useState(vehicleService?.remindWhenDue ?? false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  function validate(): boolean {
    const next: Record<string, string> = {};
    if (!serviceTypeId) next.serviceTypeId = 'Pick a service type';
    if (intervalMiles == null && intervalMonths == null) {
      next.interval = 'Set at least one interval (miles or months)';
    }
    if (intervalMiles != null && intervalMiles <= 0) next.interval = 'Interval miles must be positive';
    if (intervalMonths != null && intervalMonths <= 0) next.interval = 'Interval months must be positive';
    setErrors(next);
    return Object.keys(next).length === 0;
  }

  async function handleSave() {
    setSubmitError(null);
    if (!validate()) return;
    setSaving(true);
    try {
      await onSave({ serviceTypeId, intervalMiles, intervalMonths, remindWhenDue });
      onClose();
    } catch (e) {
      setSubmitError(errorMessage(e));
    } finally {
      setSaving(false);
    }
  }

  const options = serviceTypes.map((t) => ({
    value: t.id,
    label: t.isGlobal ? t.name : `${t.name} (custom)`,
    disabled: mode === 'create' && configuredTypeIds.includes(t.id),
  }));

  return (
    <Modal
      open={open}
      title={mode === 'create' ? 'Configure Service' : 'Edit Interval'}
      onClose={onClose}
      footer={
        <>
          <Button variant="ghost" onClick={onClose} disabled={saving}>
            Cancel
          </Button>
          <Button variant="primary" onClick={handleSave} loading={saving}>
            {mode === 'create' ? 'Add Service' : 'Save Interval'}
          </Button>
        </>
      }
    >
      {submitError && <Banner>{submitError}</Banner>}

      {mode === 'create' ? (
        <SelectField
          label="Service Type"
          value={serviceTypeId}
          onChange={setServiceTypeId}
          options={options}
          placeholder="Select a service…"
          required
          error={errors.serviceTypeId}
          hint="Already-configured services are disabled."
        />
      ) : (
        <SelectField
          label="Service Type"
          value={serviceTypeId}
          onChange={() => {}}
          options={options.filter((o) => o.value === serviceTypeId)}
          hint="Service type is fixed — edit the intervals below."
        />
      )}

      <div className="form-grid">
        <NumberField
          label="Interval — Miles"
          value={intervalMiles}
          onChange={setIntervalMiles}
          placeholder="5000"
          min={1}
        />
        <NumberField
          label="Interval — Months"
          value={intervalMonths}
          onChange={setIntervalMonths}
          placeholder="6"
          min={1}
        />
      </div>
      {errors.interval && <span className="field__error">⚠ {errors.interval}</span>}
      <span className="field__hint">
        Set either or both. Whichever comes first determines when the service is due.
      </span>

      <CheckboxField
        label="Get email reminder"
        checked={remindWhenDue}
        onChange={setRemindWhenDue}
        hint="Emails you when this service is coming due — by date or mileage."
      />
    </Modal>
  );
}
