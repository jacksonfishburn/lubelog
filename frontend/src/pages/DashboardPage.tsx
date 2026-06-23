import { Link } from 'react-router-dom';
import { useAppState } from '../context/AppState';
import { Panel } from '../components/shared/Panel';
import { LoadingBlock } from '../components/shared/Loading';
import { Banner } from '../components/shared/Banner';
import { StatusBadge } from '../components/shared/StatusBadge';
import { Button } from '../components/shared/Button';
import { EmptyState } from '../components/shared/EmptyState';
import { SERVICE_STATUS } from '../lib/serviceStatus';
import { formatVehicleName } from '../lib/format';

export function DashboardPage() {
  const { fleet } = useAppState();
  const { overviews, loading, error } = fleet;

  const totalVehicles = overviews.length;
  const overdueTotal = overviews.reduce((s, o) => s + o.overdueCount, 0);
  const dueSoonTotal = overviews.reduce((s, o) => s + o.dueSoonCount, 0);
  const serviceCount = overviews.reduce((s, o) => s + o.services.length, 0);

  const attention = overviews
    .flatMap((o) =>
      o.services
        .filter(
          (s) =>
            s.result.status === SERVICE_STATUS.OVERDUE ||
            s.result.status === SERVICE_STATUS.DUE_SOON,
        )
        .map((s) => ({
          vehicle: o.vehicle,
          service: s,
        })),
    )
    .sort((a, b) => {
      const rank = (s: typeof a.service) =>
        s.result.status === SERVICE_STATUS.OVERDUE ? 0 : 1;
      return rank(a.service) - rank(b.service);
    });

  return (
    <div className="page-grid">
      <header className="page-head">
        <div>
          <h1 className="page-head__title">Fleet Dashboard</h1>
          <p className="page-head__sub">Maintenance status across your vehicles</p>
        </div>
        <Button variant="primary" onClick={() => void fleet.refresh()}>
          Refresh
        </Button>
      </header>

      {error && <Banner>{error}</Banner>}

      {loading ? (
        <LoadingBlock label="Scanning fleet" />
      ) : (
        <>
          <div className="readout-grid">
            <div className="readout">
              <div className="readout__label">Vehicles</div>
              <div className="readout__value">{totalVehicles}</div>
            </div>
            <div className="readout">
              <div className="readout__label">Overdue</div>
              <div
                className={`readout__value${overdueTotal > 0 ? ' readout__value--accent' : ''}`}
                style={overdueTotal > 0 ? { color: 'var(--status-overdue)' } : undefined}
              >
                {overdueTotal}
              </div>
              <div className="readout__sub">services past due</div>
            </div>
            <div className="readout">
              <div className="readout__label">Due Soon</div>
              <div
                className="readout__value"
                style={dueSoonTotal > 0 ? { color: 'var(--status-due-soon)' } : undefined}
              >
                {dueSoonTotal}
              </div>
              <div className="readout__sub">within threshold</div>
            </div>
            <div className="readout">
              <div className="readout__label">Configured</div>
              <div className="readout__value">{serviceCount}</div>
              <div className="readout__sub">tracked services</div>
            </div>
          </div>

          <Panel title="Attention Queue" accent={attention.length > 0} flush>
            {attention.length === 0 ? (
              <EmptyState
                icon="◉"
                title="All Clear"
                message="No overdue or due-soon services across the fleet."
                action={
                  <Link to="/fleet" className="btn btn--default">
                    View Fleet
                  </Link>
                }
              />
            ) : (
              <>
                {attention.map(({ vehicle, service }) => (
                  <div className="svc-row" key={`${vehicle.id}-${service.vehicleService.id}`}>
                    <div className="svc-row__main">
                      <div className="svc-row__name">
                        {service.vehicleService.serviceTypeName}
                        <StatusBadge status={service.result.status} />
                      </div>
                      <div className="svc-row__meta">{formatVehicleName(vehicle)}</div>
                    </div>
                    <Link to={`/fleet/${vehicle.id}`} className="btn btn--primary btn--sm">
                      Open
                    </Link>
                  </div>
                ))}
              </>
            )}
          </Panel>

          <Panel title="Fleet Monitors">
            <div className="vehicle-grid">
              {overviews.map((o) => (
                <Link key={o.vehicle.id} to={`/fleet/${o.vehicle.id}`} className="vehicle-card-link">
                  <div className="panel vehicle-card">
                    <div className="vehicle-card__screen">
                      <span className="vehicle-card__nick">{formatVehicleName(o.vehicle)}</span>
                      <StatusBadge status={o.status} />
                    </div>
                    <div className="vehicle-card__body">
                      <div className="vehicle-card__stats">
                        <div>
                          <div className="vehicle-card__stat-l">Overdue</div>
                          <div className="vehicle-card__stat-v">{o.overdueCount}</div>
                        </div>
                        <div>
                          <div className="vehicle-card__stat-l">Due Soon</div>
                          <div className="vehicle-card__stat-v">{o.dueSoonCount}</div>
                        </div>
                        <div>
                          <div className="vehicle-card__stat-l">Services</div>
                          <div className="vehicle-card__stat-v">{o.services.length}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          </Panel>
        </>
      )}
    </div>
  );
}
