import type { ServiceStatus } from '../../lib/serviceStatus';
import { STATUS_LABEL } from '../../lib/serviceStatus';
import { StatusIcon } from './StatusIcon';

// Semantic status pill (overdue / due-soon / ok / no-history).
export function StatusBadge({ status }: { status: ServiceStatus }) {
  return (
    <span className={`status status--${status}`}>
      <StatusIcon status={status} />
      {STATUS_LABEL[status]}
    </span>
  );
}
