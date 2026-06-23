import type { VehicleOverview } from '../../hooks/useFleetOverview';
import { StatusBadge } from '../shared/StatusBadge';
import { Button } from '../shared/Button';
import { formatMileage, formatVehicleName, formatVehicleSubtitle } from '../../lib/format';

interface VehicleCardProps {
  overview: VehicleOverview;
  selected?: boolean;
  onOpen: () => void;
  onEdit: () => void;
  onDelete: () => void;
}

// A vehicle "monitor" tile for the Fleet grid: status light + key readouts.
export function VehicleCard({ overview, selected, onOpen, onEdit, onDelete }: VehicleCardProps) {
  const { vehicle, status, overdueCount, dueSoonCount, services } = overview;

  return (
    <div className={`panel vehicle-card${selected ? ' is-selected' : ''}`} onClick={onOpen}>
      <div className="vehicle-card__screen">
        <span className="vehicle-card__nick">{formatVehicleName(vehicle)}</span>
        <StatusBadge status={status} />
      </div>
      <div className="vehicle-card__body">
        <div className="vehicle-card__sub">{formatVehicleSubtitle(vehicle)}</div>

        <div className="vehicle-card__stats">
          <div>
            <div className="vehicle-card__stat-l">Odometer</div>
            <div className="vehicle-card__stat-v">{formatMileage(vehicle.mileage)}</div>
          </div>
          <div>
            <div className="vehicle-card__stat-l">Services</div>
            <div className="vehicle-card__stat-v">{services.length}</div>
          </div>
          <div>
            <div className="vehicle-card__stat-l">Attention</div>
            <div className="vehicle-card__stat-v">
              {overdueCount > 0 ? (
                <span style={{ color: 'var(--status-overdue)' }}>{overdueCount} overdue</span>
              ) : dueSoonCount > 0 ? (
                <span style={{ color: 'var(--status-due-soon)' }}>{dueSoonCount} soon</span>
              ) : (
                <span style={{ color: 'var(--status-ok)' }}>clear</span>
              )}
            </div>
          </div>
        </div>

        <div className="row right" onClick={(e) => e.stopPropagation()} style={{ gap: 'var(--sp-2)' }}>
          <Button size="sm" variant="ghost" onClick={onEdit}>
            Edit
          </Button>
          <Button size="sm" variant="ghost" onClick={onDelete}>
            ✕
          </Button>
        </div>
      </div>
    </div>
  );
}
