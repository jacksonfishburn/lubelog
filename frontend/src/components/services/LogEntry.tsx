import type { ServiceLog } from '../../types';
import { Button } from '../shared/Button';
import { formatCurrency, formatDate, formatMileage } from '../../lib/format';

interface LogEntryProps {
  log: ServiceLog;
  serviceName: string;
  onEdit?: () => void;
  onDelete?: () => void;
  onDeleteDetail?: (detailId: string) => void;
}

export function LogEntry({ log, serviceName, onEdit, onDelete, onDeleteDetail }: LogEntryProps) {
  return (
    <article className="log-entry">
      <div className="log-entry__head">
        <span className="log-entry__name">{serviceName}</span>
        <span className="log-entry__date">{formatDate(log.doneAtDate)}</span>
        {(onEdit || onDelete) && (
          <div className="row right" style={{ gap: 'var(--sp-2)' }}>
            {onEdit && (
              <Button size="sm" variant="ghost" onClick={onEdit}>
                Edit
              </Button>
            )}
            {onDelete && (
              <Button size="sm" variant="ghost" onClick={onDelete}>
                ✕
              </Button>
            )}
          </div>
        )}
      </div>
      <div className="log-entry__meta">
        {log.doneAtMileage != null && <span>{formatMileage(log.doneAtMileage)}</span>}
        {log.cost != null && <span>{formatCurrency(log.cost)}</span>}
        {log.mileageDue != null && <span>Next @ {formatMileage(log.mileageDue)}</span>}
        {log.dateDue != null && <span>Due {formatDate(log.dateDue)}</span>}
      </div>
      {log.notes && <p className="log-entry__notes">{log.notes}</p>}
      {log.details.length > 0 && (
        <div className="kv-list">
          {log.details.map((d) => (
            <span className="kv" key={d.id}>
              <span className="kv__k">{d.key}</span>
              <span className="kv__v">{d.value}</span>
              {onDeleteDetail && (
                <button
                  type="button"
                  className="kv__del"
                  aria-label={`Remove ${d.key}`}
                  onClick={() => onDeleteDetail(d.id)}
                >
                  ×
                </button>
              )}
            </span>
          ))}
        </div>
      )}
    </article>
  );
}
