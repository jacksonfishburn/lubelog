import type { ServiceLog } from '../types';

// Service status is DERIVED on the client from the backend's computed
// `mileageDue` / `dateDue` (returned on each LogResponse) plus the vehicle's
// current mileage and today's date. Keeping it here makes it unit-testable and
// the single source of truth for every status pill, gauge, and sort in the UI.

export const SERVICE_STATUS = {
  OVERDUE: 'overdue',
  DUE_SOON: 'due-soon',
  OK: 'ok',
  NO_HISTORY: 'no-history',
} as const;

export type ServiceStatus = (typeof SERVICE_STATUS)[keyof typeof SERVICE_STATUS];

// "Due soon" thresholds — within this much of the due point.
export const DUE_SOON_MILES = 500;
export const DUE_SOON_DAYS = 30;

export interface ServiceStatusResult {
  status: ServiceStatus;
  /** Latest log used for the calculation, or null when there's no history. */
  latestLog: ServiceLog | null;
  /** Miles until due (negative = overdue). Null when no mileage interval. */
  milesRemaining: number | null;
  /** Days until due (negative = overdue). Null when no date interval. */
  daysRemaining: number | null;
}

// Picks the most recent log: latest done date wins, mileage breaks ties.
export function latestLog(logs: ServiceLog[]): ServiceLog | null {
  if (logs.length === 0) return null;
  return [...logs].sort((a, b) => {
    if (a.doneAtDate !== b.doneAtDate) {
      return a.doneAtDate < b.doneAtDate ? 1 : -1;
    }
    return (b.doneAtMileage ?? 0) - (a.doneAtMileage ?? 0);
  })[0];
}

function parseLocalDate(isoDate: string): Date {
  const [y, m, d] = isoDate.split('-').map(Number);
  return new Date(y, m - 1, d);
}

function daysBetween(from: Date, to: Date): number {
  const ms = 24 * 60 * 60 * 1000;
  const a = new Date(from.getFullYear(), from.getMonth(), from.getDate()).getTime();
  const b = new Date(to.getFullYear(), to.getMonth(), to.getDate()).getTime();
  return Math.round((b - a) / ms);
}

export function computeServiceStatus(
  logs: ServiceLog[],
  currentMileage: number | null,
  today: Date = new Date(),
): ServiceStatusResult {
  const latest = latestLog(logs);
  if (!latest) {
    return { status: SERVICE_STATUS.NO_HISTORY, latestLog: null, milesRemaining: null, daysRemaining: null };
  }

  const milesRemaining =
    latest.mileageDue != null && currentMileage != null ? latest.mileageDue - currentMileage : null;
  const daysRemaining =
    latest.dateDue != null ? daysBetween(today, parseLocalDate(latest.dateDue)) : null;

  const overdue =
    (milesRemaining != null && milesRemaining <= 0) || (daysRemaining != null && daysRemaining <= 0);

  if (overdue) {
    return { status: SERVICE_STATUS.OVERDUE, latestLog: latest, milesRemaining, daysRemaining };
  }

  const dueSoon =
    (milesRemaining != null && milesRemaining <= DUE_SOON_MILES) ||
    (daysRemaining != null && daysRemaining <= DUE_SOON_DAYS);

  return {
    status: dueSoon ? SERVICE_STATUS.DUE_SOON : SERVICE_STATUS.OK,
    latestLog: latest,
    milesRemaining,
    daysRemaining,
  };
}

const SEVERITY: Record<ServiceStatus, number> = {
  [SERVICE_STATUS.OVERDUE]: 3,
  [SERVICE_STATUS.DUE_SOON]: 2,
  [SERVICE_STATUS.OK]: 1,
  [SERVICE_STATUS.NO_HISTORY]: 0,
};

// The "headline" status for a vehicle = its most severe service status.
export function worstStatus(statuses: ServiceStatus[]): ServiceStatus {
  if (statuses.length === 0) return SERVICE_STATUS.NO_HISTORY;
  return statuses.reduce((worst, s) => (SEVERITY[s] > SEVERITY[worst] ? s : worst));
}

export const STATUS_LABEL: Record<ServiceStatus, string> = {
  [SERVICE_STATUS.OVERDUE]: 'Overdue',
  [SERVICE_STATUS.DUE_SOON]: 'Due Soon',
  [SERVICE_STATUS.OK]: 'OK',
  [SERVICE_STATUS.NO_HISTORY]: 'No History',
};
