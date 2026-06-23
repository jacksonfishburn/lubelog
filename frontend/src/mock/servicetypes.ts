// ── MOCK DATA LAYER: service types ───────────────────────────────────────────
// Mirrors GET/POST/DELETE /api/service-types. Globals (is_global=true, seeded in
// V7) plus the current user's custom types.
//
// TO GO REAL: swap each function body for a fetch against `${API_BASE}/service-types`.
// const API_BASE = '/api'; // Vite proxies /api → http://localhost:8081
import type { CreateServiceTypeRequest, ServiceType } from '../types';
import { delay, delayReject } from '../lib/latency';
import { newId } from '../lib/id';

// Stable ids so vehicle-service configs can reference these by hand below.
export const SERVICE_TYPE_IDS = {
  oilChange: '20000000-0000-0000-0000-000000000001',
  tireRotation: '20000000-0000-0000-0000-000000000002',
  airFilter: '20000000-0000-0000-0000-000000000003',
  cabinAirFilter: '20000000-0000-0000-0000-000000000004',
  transmission: '20000000-0000-0000-0000-000000000005',
  brakeInspection: '20000000-0000-0000-0000-000000000006',
  coolantFlush: '20000000-0000-0000-0000-000000000007',
  sparkPlugs: '20000000-0000-0000-0000-000000000008',
  differential: '20000000-0000-0000-0000-000000000009',
  fuelFilter: '20000000-0000-0000-0000-00000000000a',
  // custom (user-defined) types
  wiperBlades: '20000000-0000-0000-0000-0000000000f1',
  batteryReplacement: '20000000-0000-0000-0000-0000000000f2',
} as const;

const SEED: ServiceType[] = [
  { id: SERVICE_TYPE_IDS.oilChange, name: 'Oil Change', isGlobal: true, createdAt: '2026-01-01T00:00:00Z' },
  { id: SERVICE_TYPE_IDS.tireRotation, name: 'Tire Rotation', isGlobal: true, createdAt: '2026-01-01T00:00:00Z' },
  { id: SERVICE_TYPE_IDS.airFilter, name: 'Air Filter', isGlobal: true, createdAt: '2026-01-01T00:00:00Z' },
  { id: SERVICE_TYPE_IDS.cabinAirFilter, name: 'Cabin Air Filter', isGlobal: true, createdAt: '2026-01-01T00:00:00Z' },
  { id: SERVICE_TYPE_IDS.transmission, name: 'Transmission Service', isGlobal: true, createdAt: '2026-01-01T00:00:00Z' },
  { id: SERVICE_TYPE_IDS.brakeInspection, name: 'Brake Inspection', isGlobal: true, createdAt: '2026-01-01T00:00:00Z' },
  { id: SERVICE_TYPE_IDS.coolantFlush, name: 'Coolant Flush', isGlobal: true, createdAt: '2026-01-01T00:00:00Z' },
  { id: SERVICE_TYPE_IDS.sparkPlugs, name: 'Spark Plugs', isGlobal: true, createdAt: '2026-01-01T00:00:00Z' },
  { id: SERVICE_TYPE_IDS.differential, name: 'Differential Service', isGlobal: true, createdAt: '2026-01-01T00:00:00Z' },
  { id: SERVICE_TYPE_IDS.fuelFilter, name: 'Fuel Filter', isGlobal: true, createdAt: '2026-01-01T00:00:00Z' },
  // user custom types
  { id: SERVICE_TYPE_IDS.wiperBlades, name: 'Wiper Blades', isGlobal: false, createdAt: '2026-02-12T15:30:00Z' },
  { id: SERVICE_TYPE_IDS.batteryReplacement, name: 'Battery Replacement', isGlobal: false, createdAt: '2026-03-08T09:05:00Z' },
];

// In-memory store (mutable for the session). Globals sort first, then by name.
const store: ServiceType[] = [...SEED];

function sorted(types: ServiceType[]): ServiceType[] {
  return [...types].sort((a, b) => {
    if (a.isGlobal !== b.isGlobal) return a.isGlobal ? -1 : 1;
    return a.name.localeCompare(b.name);
  });
}

export async function listServiceTypes(): Promise<ServiceType[]> {
  return delay(sorted(store).map((t) => ({ ...t })));
}

export async function createServiceType(req: CreateServiceTypeRequest): Promise<ServiceType> {
  const name = req.name.trim();
  // Backend duplicate guard: case-insensitive name collision (global or own).
  const dup = store.some((t) => t.name.toLowerCase() === name.toLowerCase());
  if (dup) {
    return delayReject(new Error(`A service type named "${name}" already exists`));
  }
  const created: ServiceType = {
    id: newId(),
    name,
    isGlobal: false,
    createdAt: new Date().toISOString(),
  };
  store.push(created);
  return delay({ ...created });
}

export async function deleteServiceType(id: string): Promise<void> {
  const target = store.find((t) => t.id === id);
  if (!target) return delayReject(new Error('Service type not found'));
  // Backend rejects deleting a global type.
  if (target.isGlobal) return delayReject(new Error('Global service types cannot be deleted'));
  store.splice(store.indexOf(target), 1);
  return delay(undefined);
}
