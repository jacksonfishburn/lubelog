import { useEffect, useMemo, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useAppState } from '../context/AppState';
import { useServiceTypes } from '../hooks/useServiceTypes';
import { useFindParts } from '../hooks/useFindParts';
import { PartsResultList } from '../components/ai/PartsResultList';
import { Panel } from '../components/shared/Panel';
import { Button } from '../components/shared/Button';
import { Banner } from '../components/shared/Banner';
import { LoadingBlock } from '../components/shared/Loading';
import { EmptyState } from '../components/shared/EmptyState';
import { SelectField } from '../components/shared/fields';
import { formatVehicleName } from '../lib/format';

export function AiFindPartsPage() {
  const [searchParams] = useSearchParams();
  const { selectedVehicleId, setSelectedVehicleId, fleet } = useAppState();
  const { serviceTypes, loading: typesLoading, error: typesError } = useServiceTypes();
  const { parts, loading, error, find } = useFindParts();

  const activeId = selectedVehicleId ?? fleet.overviews[0]?.vehicle.id ?? null;
  const [serviceTypeId, setServiceTypeId] = useState('');
  const autoRan = useRef(false);

  const typeOptions = useMemo(
    () => serviceTypes.map((t) => ({ value: t.id, label: t.name })),
    [serviceTypes],
  );

  // Hydrate from query params once (cockpit deep-link).
  useEffect(() => {
    const qVehicle = searchParams.get('vehicleId');
    const qType = searchParams.get('serviceTypeId');
    if (qVehicle) setSelectedVehicleId(qVehicle);
    if (qType) setServiceTypeId(qType);
  }, [searchParams, setSelectedVehicleId]);

  // Auto-fetch once when both IDs are available from the deep-link.
  useEffect(() => {
    if (autoRan.current) return;
    const qVehicle = searchParams.get('vehicleId');
    const qType = searchParams.get('serviceTypeId');
    if (!qVehicle || !qType) return;
    autoRan.current = true;
    void find(qVehicle, qType);
  }, [searchParams, find]);

  const canSubmit = Boolean(activeId && serviceTypeId) && !loading;

  function handleFind() {
    if (!activeId || !serviceTypeId) return;
    void find(activeId, serviceTypeId);
  }

  return (
    <div className="page-grid">
      <header className="page-head">
        <div>
          <h1 className="page-head__title">AI Find Parts</h1>
          <p className="page-head__sub">
            Suggest purchasable parts for a service on a vehicle
          </p>
        </div>
      </header>

      {(typesError || fleet.error) && (
        <Banner>{typesError ?? fleet.error}</Banner>
      )}

      {fleet.overviews.length > 0 && (
        <div className="vsel">
          {fleet.overviews.map((o) => (
            <button
              key={o.vehicle.id}
              type="button"
              className={`vsel__btn${activeId === o.vehicle.id ? ' is-active' : ''}`}
              onClick={() => setSelectedVehicleId(o.vehicle.id)}
              disabled={loading}
            >
              {formatVehicleName(o.vehicle)}
            </button>
          ))}
        </div>
      )}

      {fleet.overviews.length === 0 && !fleet.loading ? (
        <EmptyState
          icon="⊞"
          title="No Vehicles"
          message="Add a vehicle to the fleet before looking up parts."
        />
      ) : (
        <Panel title="Lookup">
          <div className="ai-find-form">
            <SelectField
              label="Service Type"
              value={serviceTypeId}
              onChange={setServiceTypeId}
              options={typeOptions}
              placeholder={typesLoading ? 'Loading…' : 'Select a service type'}
              required
            />
            <p className="ai-find-form__disclaimer">
              AI suggestions can be wrong. Double-check fitment, specs, and retailer links
              before buying.
            </p>
            <div className="ai-find-form__actions">
              <Button
                variant="primary"
                onClick={handleFind}
                disabled={!canSubmit}
                loading={loading}
              >
                Find Parts
              </Button>
            </div>
          </div>
        </Panel>
      )}

      <Panel title="Results" flush>
        {loading ? (
          <LoadingBlock label="Finding parts — this can take a moment" />
        ) : error ? (
          <div className="parts-list__state">
            <Banner>{error}</Banner>
            <p className="parts-list__hint">Adjust your selection and try again.</p>
          </div>
        ) : parts == null ? (
          <EmptyState
            icon="◈"
            title="No Results Yet"
            message="Select a vehicle and service type, then hit Find Parts."
          />
        ) : parts.length === 0 ? (
          <EmptyState
            icon="◈"
            title="No Parts Found"
            message="The lookup completed but returned no parts. Try a different service type."
          />
        ) : (
          <PartsResultList parts={parts} />
        )}
      </Panel>
    </div>
  );
}
