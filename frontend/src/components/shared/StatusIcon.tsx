import { SERVICE_STATUS, type ServiceStatus } from '../../lib/serviceStatus';

interface StatusIconProps {
  status: ServiceStatus;
  size?: 'sm' | 'md';
}

// Small retro marks — distinct shape per status (no generic glowing dots).
export function StatusIcon({ status, size = 'sm' }: StatusIconProps) {
  const cls = `status-icon status-icon--${status}${size === 'md' ? ' status-icon--md' : ''}`;

  switch (status) {
    case SERVICE_STATUS.OVERDUE:
      return (
        <svg className={cls} viewBox="0 0 12 12" aria-hidden>
          <path d="M6 2.5 L6 7" stroke="currentColor" strokeWidth="1.75" strokeLinecap="square" />
          <circle cx="6" cy="9.25" r="0.9" fill="currentColor" />
        </svg>
      );
    case SERVICE_STATUS.DUE_SOON:
      return (
        <svg className={cls} viewBox="0 0 12 12" aria-hidden>
          <circle cx="6" cy="6" r="4.25" stroke="currentColor" strokeWidth="1.5" fill="none" />
          <path d="M6 6 L6 3.75" stroke="currentColor" strokeWidth="1.5" strokeLinecap="square" />
          <path d="M6 6 L8 7.25" stroke="currentColor" strokeWidth="1.5" strokeLinecap="square" />
        </svg>
      );
    case SERVICE_STATUS.OK:
      return (
        <svg className={cls} viewBox="0 0 12 12" aria-hidden>
          <path
            d="M2.5 6.25 L5 8.75 L9.5 3.75"
            stroke="currentColor"
            strokeWidth="1.75"
            fill="none"
            strokeLinecap="square"
            strokeLinejoin="miter"
          />
        </svg>
      );
    case SERVICE_STATUS.NO_HISTORY:
      return (
        <svg className={cls} viewBox="0 0 12 12" aria-hidden>
          <path d="M2.75 6 H9.25" stroke="currentColor" strokeWidth="1.75" strokeLinecap="square" />
        </svg>
      );
    default:
      return null;
  }
}

export function gaugeClassForStatus(status: ServiceStatus): string {
  switch (status) {
    case SERVICE_STATUS.OVERDUE:
      return ' gauge--overdue';
    case SERVICE_STATUS.DUE_SOON:
      return ' gauge--due-soon';
    case SERVICE_STATUS.OK:
      return ' gauge--ok';
    default:
      return ' gauge--none';
  }
}
