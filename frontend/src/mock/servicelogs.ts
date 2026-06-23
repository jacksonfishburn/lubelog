// ── MOCK DATA LAYER: service logs + flexible key/value details ───────────────
// Mirrors /api/logs (GET by vehicleServiceId|vehicleId, POST, GET one, PUT,
// DELETE) and /api/logs/{logId}/details + /api/logs/details/{detailId}.
//
// IMPORTANT parity with the backend:
//  - mileageDue / dateDue are COMPUTED on read from the vehicle-service interval,
//    never stored (see toResponse()).
//  - Logging can advance the vehicle's odometer; a log below current mileage is
//    rejected (InvalidServiceLogMileageException).
//
// TO GO REAL: fetch against `${API_BASE}/logs`. const API_BASE = '/api';
import type { CreateLogRequest, LogDetail, LogDetailRequest, ServiceLog } from '../types';
import { delay, delayReject } from '../lib/latency';
import { newId } from '../lib/id';
import { getVehicleSync } from './vehicles';
import {
  VEHICLE_SERVICE_IDS,
  getVehicleServiceSync,
  vehicleServicesForVehicleSync,
} from './vehicleservices';

// Stored shape: the raw row. Computed fields are derived in toResponse().
type RawLog = Omit<ServiceLog, 'mileageDue' | 'dateDue'>;

function detail(id: string, key: string, value: string): LogDetail {
  return { id, key, value };
}

const SEED: RawLog[] = [
  // Tacoma — Oil Change (ok: 2,500 mi / months remaining)
  { id: '50000000-0000-0000-0000-000000000001', vehicleServiceId: VEHICLE_SERVICE_IDS.tacomaOil, doneAtMileage: 70200, doneAtDate: '2025-10-12', cost: 62.5, notes: 'Mobil 1 full synthetic', details: [detail('51000000-0000-0000-0000-000000000001', 'Oil Type', '0W-20 Full Synthetic'), detail('51000000-0000-0000-0000-000000000002', 'Filter', 'OEM 90915-YZZF2'), detail('51000000-0000-0000-0000-000000000003', 'Quarts', '6.1')] },
  { id: '50000000-0000-0000-0000-000000000002', vehicleServiceId: VEHICLE_SERVICE_IDS.tacomaOil, doneAtMileage: 75500, doneAtDate: '2026-04-10', cost: 68.0, notes: null, details: [detail('51000000-0000-0000-0000-000000000004', 'Oil Type', '0W-20 Full Synthetic'), detail('51000000-0000-0000-0000-000000000005', 'Filter', 'OEM')] },
  // Tacoma — Tire Rotation (overdue by mileage: due 76,500, at 78,000)
  { id: '50000000-0000-0000-0000-000000000003', vehicleServiceId: VEHICLE_SERVICE_IDS.tacomaTires, doneAtMileage: 69000, doneAtDate: '2025-08-20', cost: 0, notes: 'Rotated front-to-back, torqued to 83 lb-ft', details: [detail('51000000-0000-0000-0000-000000000006', 'Pattern', 'Front-to-back')] },
  // Tacoma — Brake Inspection (ok by date: due 2026-09-01)
  { id: '50000000-0000-0000-0000-000000000004', vehicleServiceId: VEHICLE_SERVICE_IDS.tacomaBrakes, doneAtMileage: 71000, doneAtDate: '2025-09-01', cost: 0, notes: 'Pads + rotors inspected, all within spec', details: [detail('51000000-0000-0000-0000-000000000007', 'Front Pads', '7mm'), detail('51000000-0000-0000-0000-000000000008', 'Rear Pads', '8mm')] },
  // Tacoma — Cabin Air Filter (due soon: due 78,200, at 78,000)
  { id: '50000000-0000-0000-0000-000000000005', vehicleServiceId: VEHICLE_SERVICE_IDS.tacomaCabin, doneAtMileage: 63200, doneAtDate: '2025-07-15', cost: 24.99, notes: null, details: [detail('51000000-0000-0000-0000-000000000009', 'Part', 'FRAM CF10134')] },

  // Civic — Oil Change (ok)
  { id: '50000000-0000-0000-0000-000000000006', vehicleServiceId: VEHICLE_SERVICE_IDS.civicOil, doneAtMileage: 33900, doneAtDate: '2025-12-20', cost: 59.0, notes: null, details: [detail('51000000-0000-0000-0000-00000000000a', 'Oil Type', '0W-20')] },
  { id: '50000000-0000-0000-0000-000000000007', vehicleServiceId: VEHICLE_SERVICE_IDS.civicOil, doneAtMileage: 40800, doneAtDate: '2026-06-05', cost: 61.5, notes: 'Dealer service', details: [detail('51000000-0000-0000-0000-00000000000b', 'Oil Type', '0W-20'), detail('51000000-0000-0000-0000-00000000000c', 'Filter', 'OEM')] },
  // Civic — Tire Rotation (due soon: due 41,400, at 41,200)
  { id: '50000000-0000-0000-0000-000000000008', vehicleServiceId: VEHICLE_SERVICE_IDS.civicTires, doneAtMileage: 35400, doneAtDate: '2025-10-01', cost: 0, notes: 'Rotated', details: [] },
  // Civic — Transmission Service: intentionally NO logs → "no history"

  // Outback — Oil Change (overdue by date: due 2026-05-15)
  { id: '50000000-0000-0000-0000-000000000009', vehicleServiceId: VEHICLE_SERVICE_IDS.outbackOil, doneAtMileage: 122000, doneAtDate: '2025-05-20', cost: 70.0, notes: null, details: [detail('51000000-0000-0000-0000-00000000000d', 'Oil Type', '5W-30')] },
  { id: '50000000-0000-0000-0000-00000000000a', vehicleServiceId: VEHICLE_SERVICE_IDS.outbackOil, doneAtMileage: 128000, doneAtDate: '2025-11-15', cost: 72.5, notes: 'Synthetic', details: [detail('51000000-0000-0000-0000-00000000000e', 'Oil Type', '5W-30 Full Synthetic'), detail('51000000-0000-0000-0000-00000000000f', 'Filter', 'OEM')] },
  // Outback — Spark Plugs (ok)
  { id: '50000000-0000-0000-0000-00000000000b', vehicleServiceId: VEHICLE_SERVICE_IDS.outbackPlugs, doneAtMileage: 41000, doneAtDate: '2020-06-10', cost: 180.0, notes: 'NGK Iridium', details: [detail('51000000-0000-0000-0000-000000000010', 'Plugs', 'NGK Iridium x6')] },
  // Outback — Battery Replacement (custom type, ok by date: due 2028-03-01)
  { id: '50000000-0000-0000-0000-00000000000c', vehicleServiceId: VEHICLE_SERVICE_IDS.outbackBattery, doneAtMileage: 96000, doneAtDate: '2024-03-01', cost: 199.99, notes: 'Interstate MTP-24F', details: [detail('51000000-0000-0000-0000-000000000011', 'Brand', 'Interstate'), detail('51000000-0000-0000-0000-000000000012', 'Group Size', '24F')] },
];

const store: RawLog[] = SEED.map((l) => ({ ...l, details: [...l.details] }));

// LocalDate + months, clamping the day to the target month (matches Java's
// LocalDate.plusMonths month-end behavior rather than JS Date rollover).
function addMonths(isoDate: string, months: number): string {
  const [y, m, d] = isoDate.split('-').map(Number);
  const base = new Date(y, m - 1 + months, 1);
  const daysInMonth = new Date(base.getFullYear(), base.getMonth() + 1, 0).getDate();
  const day = Math.min(d, daysInMonth);
  const yyyy = base.getFullYear();
  const mm = String(base.getMonth() + 1).padStart(2, '0');
  const dd = String(day).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

// Computes the response, deriving mileageDue / dateDue from the live interval.
function toResponse(raw: RawLog): ServiceLog {
  const vs = getVehicleServiceSync(raw.vehicleServiceId);
  const mileageDue =
    raw.doneAtMileage != null && vs?.intervalMiles != null ? raw.doneAtMileage + vs.intervalMiles : null;
  const dateDue = vs?.intervalMonths != null ? addMonths(raw.doneAtDate, vs.intervalMonths) : null;
  return {
    id: raw.id,
    vehicleServiceId: raw.vehicleServiceId,
    doneAtMileage: raw.doneAtMileage,
    mileageDue,
    doneAtDate: raw.doneAtDate,
    dateDue,
    cost: raw.cost,
    notes: raw.notes,
    details: raw.details.map((x) => ({ ...x })),
  };
}

function byDateDesc(a: ServiceLog, b: ServiceLog): number {
  if (a.doneAtDate !== b.doneAtDate) return a.doneAtDate < b.doneAtDate ? 1 : -1;
  return (b.doneAtMileage ?? 0) - (a.doneAtMileage ?? 0);
}

export async function listLogsByVehicleService(vehicleServiceId: string): Promise<ServiceLog[]> {
  const logs = store
    .filter((l) => l.vehicleServiceId === vehicleServiceId)
    .map(toResponse)
    .sort(byDateDesc);
  return delay(logs);
}

export async function listLogsByVehicle(vehicleId: string): Promise<ServiceLog[]> {
  const vsIds = new Set(vehicleServicesForVehicleSync(vehicleId).map((vs) => vs.id));
  const logs = store
    .filter((l) => vsIds.has(l.vehicleServiceId))
    .map(toResponse)
    .sort(byDateDesc);
  return delay(logs);
}

export async function getLog(logId: string): Promise<ServiceLog> {
  const raw = store.find((l) => l.id === logId);
  if (!raw) return delayReject(new Error('Log not found'));
  return delay(toResponse(raw));
}

export async function createLog(req: CreateLogRequest): Promise<ServiceLog> {
  const vs = getVehicleServiceSync(req.vehicleServiceId);
  if (!vs) return delayReject(new Error('Vehicle service not found'));

  const vehicle = getVehicleSync(vs.vehicleId);
  // Odometer rule: reject a log below the vehicle's current mileage.
  if (req.doneAtMileage != null && vehicle?.mileage != null && req.doneAtMileage < vehicle.mileage) {
    return delayReject(
      new Error(`Mileage ${req.doneAtMileage.toLocaleString()} is below the vehicle's current ${vehicle.mileage.toLocaleString()}`),
    );
  }

  const raw: RawLog = {
    id: newId(),
    vehicleServiceId: req.vehicleServiceId,
    doneAtMileage: req.doneAtMileage ?? null,
    doneAtDate: req.doneAtDate,
    cost: req.cost ?? null,
    notes: req.notes ?? null,
    details: (req.details ?? []).map((dq) => ({ id: newId(), key: dq.key, value: dq.value })),
  };
  store.push(raw);

  // Advance odometer if this log is higher than current.
  if (vehicle && req.doneAtMileage != null && (vehicle.mileage == null || req.doneAtMileage > vehicle.mileage)) {
    vehicle.mileage = req.doneAtMileage;
  }
  return delay(toResponse(raw));
}

export async function updateLog(logId: string, req: CreateLogRequest): Promise<ServiceLog> {
  const raw = store.find((l) => l.id === logId);
  if (!raw) return delayReject(new Error('Log not found'));
  raw.vehicleServiceId = req.vehicleServiceId;
  raw.doneAtMileage = req.doneAtMileage ?? null;
  raw.doneAtDate = req.doneAtDate;
  raw.cost = req.cost ?? null;
  raw.notes = req.notes ?? null;
  if (req.details) {
    raw.details = req.details.map((dq) => ({ id: newId(), key: dq.key, value: dq.value }));
  }

  // Keep odometer consistent if the edit pushed mileage higher.
  const vs = getVehicleServiceSync(raw.vehicleServiceId);
  const vehicle = vs ? getVehicleSync(vs.vehicleId) : undefined;
  if (vehicle && raw.doneAtMileage != null && (vehicle.mileage == null || raw.doneAtMileage > vehicle.mileage)) {
    vehicle.mileage = raw.doneAtMileage;
  }
  return delay(toResponse(raw));
}

export async function deleteLog(logId: string): Promise<void> {
  const idx = store.findIndex((l) => l.id === logId);
  if (idx === -1) return delayReject(new Error('Log not found'));
  store.splice(idx, 1);
  return delay(undefined);
}

export async function addDetail(logId: string, req: LogDetailRequest): Promise<LogDetail> {
  const raw = store.find((l) => l.id === logId);
  if (!raw) return delayReject(new Error('Log not found'));
  // Backend unique (service_log_id, key).
  if (raw.details.some((d) => d.key === req.key)) {
    return delayReject(new Error(`A detail with key "${req.key}" already exists on this log`));
  }
  const created: LogDetail = { id: newId(), key: req.key, value: req.value };
  raw.details.push(created);
  return delay({ ...created });
}

export async function deleteDetail(detailId: string): Promise<void> {
  for (const raw of store) {
    const idx = raw.details.findIndex((d) => d.id === detailId);
    if (idx !== -1) {
      raw.details.splice(idx, 1);
      return delay(undefined);
    }
  }
  return delayReject(new Error('Detail not found'));
}

// Internal helper for client-side validation: the highest mileage ever logged
// against any of a vehicle's services. Used to stop edits dropping below it.
export function highestLoggedMileageForVehicle(vehicleId: string): number | null {
  const vsIds = new Set(vehicleServicesForVehicleSync(vehicleId).map((vs) => vs.id));
  let max: number | null = null;
  for (const l of store) {
    if (vsIds.has(l.vehicleServiceId) && l.doneAtMileage != null) {
      max = max == null ? l.doneAtMileage : Math.max(max, l.doneAtMileage);
    }
  }
  return max;
}
