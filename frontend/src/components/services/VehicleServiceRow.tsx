import type { ServiceWithStatus } from '../../hooks/useFleetOverview';
import { SERVICE_STATUS } from '../../lib/serviceStatus';
import { StatusBadge } from '../shared/StatusBadge';
import { Button } from '../shared/Button';
import { formatDate, formatInterval, formatMileage } from '../../lib/format';

interface VehicleServiceRowProps {
  item: ServiceWithStatus;
  onLog: () => void;
  onEdit: () => void;
  onDelete: () => void;
}

function dueText(item: ServiceWithStatus): string {
  const { result } = item;
  const latest = result.latestLog;
  if (!latest) return 'No history yet';
  const parts: string[] = [];
  if (latest.mileageDue != null) parts.push(formatMileage(latest.mileageDue));
  if (latest.dateDue != null) parts.push(formatDate(latest.dateDue));
  return parts.length ? `Due ${parts.join(' · ')}` : 'No interval';
}

function remainingText(item: ServiceWithStatus): string | null {
  const { milesRemaining, daysRemaining } = item.result;
  const bits: string[] = [];
  if (milesRemaining != null) {
    bits.push(milesRemaining < 0 ? `${Math.abs(milesRemaining).toLocaleString()} mi over` : `${milesRemaining.toLocaleString()} mi left`);
  }
  if (daysRemaining != null) {
    bits.push(daysRemaining < 0 ? `${Math.abs(daysRemaining)} d over` : `${daysRemaining} d left`);
  }
  return bits.length ? bits.join(' · ') : null;
}

export function VehicleServiceRow({ item, onLog, onEdit, onDelete }: VehicleServiceRowProps) {
  const { vehicleService, result } = item;
  const rowClass =
    result.status === SERVICE_STATUS.OVERDUE
      ? ' is-overdue'
      : result.status === SERVICE_STATUS.DUE_SOON
        ? ' is-due-soon'
        : '';
  const remaining = remainingText(item);

  return (
    <div className={`svc-row${rowClass}`}>
      <div className="svc-row__main">
        <div className="svc-row__name">
          {vehicleService.serviceTypeName}
          <StatusBadge status={result.status} />
        </div>
        <div className="svc-row__meta">
          {formatInterval(vehicleService.intervalMiles, vehicleService.intervalMonths)}
          {result.latestLog && (
            <>
              {' · last '}
              {formatDate(result.latestLog.doneAtDate)}
              {result.latestLog.doneAtMileage != null && ` @ ${formatMileage(result.latestLog.doneAtMileage)}`}
            </>
          )}
        </div>
      </div>

      <div className="svc-row__due">
        <div>{dueText(item)}</div>
        {remaining && <div className="mono" style={{ fontSize: 11 }}>{remaining}</div>}
      </div>

      <div className="svc-row__actions">
        <Button size="sm" variant="primary" onClick={onLog}>
          Log
        </Button>
        <Button size="sm" variant="ghost" onClick={onEdit}>
          Edit
        </Button>
        <Button size="sm" variant="ghost" onClick={onDelete}>
          ✕
        </Button>
      </div>
    </div>
  );
}
