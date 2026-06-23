// ── MOCK DATA LAYER: vehicles + VIN lookup ───────────────────────────────────
// Mirrors /api/vehicles (GET list, GET one, POST, PATCH, DELETE) and
// /api/vin/{vin} (best-effort NHTSA decode).
//
// TO GO REAL: swap bodies for fetch against `${API_BASE}/vehicles` and
// `${API_BASE}/vin/{vin}`. const API_BASE = '/api'; // Vite proxies to :8081
import type {
  CreateVehicleRequest,
  UpdateVehicleRequest,
  Vehicle,
  VinDecodeResult,
} from '../types';
import { delay, delayReject } from '../lib/latency';
import { newId } from '../lib/id';
import { MOCK_USER_ID } from './user';

export const VEHICLE_IDS = {
  tacoma: '30000000-0000-0000-0000-000000000001',
  civic: '30000000-0000-0000-0000-000000000002',
  outback: '30000000-0000-0000-0000-000000000003',
} as const;

const SEED: Vehicle[] = [
  {
    id: VEHICLE_IDS.tacoma,
    userId: MOCK_USER_ID,
    year: 2019,
    make: 'Toyota',
    model: 'Tacoma',
    trim: 'TRD Off-Road',
    vin: '3TMCZ5AN6KM254187',
    nickname: 'Work Truck',
    mileage: 78000,
    createdAt: '2026-01-05T14:10:00Z',
  },
  {
    id: VEHICLE_IDS.civic,
    userId: MOCK_USER_ID,
    year: 2021,
    make: 'Honda',
    model: 'Civic',
    trim: 'EX',
    vin: '19XFC1F39ME201145',
    nickname: 'Daily',
    mileage: 41200,
    createdAt: '2026-01-06T09:42:00Z',
  },
  {
    id: VEHICLE_IDS.outback,
    userId: MOCK_USER_ID,
    year: 2015,
    make: 'Subaru',
    model: 'Outback',
    trim: '3.6R Limited',
    vin: '4S4BSENC2F3261904',
    nickname: 'Adventure Rig',
    mileage: 132500,
    createdAt: '2026-01-09T17:55:00Z',
  },
];

const store: Vehicle[] = [...SEED];

export async function listVehicles(): Promise<Vehicle[]> {
  return delay(store.map((v) => ({ ...v })));
}

export async function getVehicle(id: string): Promise<Vehicle> {
  const v = store.find((x) => x.id === id);
  if (!v) return delayReject(new Error('Vehicle not found'));
  return delay({ ...v });
}

export async function createVehicle(req: CreateVehicleRequest): Promise<Vehicle> {
  const created: Vehicle = {
    id: newId(),
    userId: MOCK_USER_ID,
    year: req.year ?? null,
    make: req.make ?? null,
    model: req.model ?? null,
    trim: req.trim ?? null,
    vin: req.vin ?? null,
    nickname: req.nickname ?? null,
    mileage: req.mileage ?? null,
    createdAt: new Date().toISOString(),
  };
  store.push(created);
  return delay({ ...created });
}

export async function updateVehicle(id: string, req: UpdateVehicleRequest): Promise<Vehicle> {
  const v = store.find((x) => x.id === id);
  if (!v) return delayReject(new Error('Vehicle not found'));
  // PATCH semantics: only apply keys that were provided.
  if (req.nickname !== undefined) v.nickname = req.nickname;
  if (req.mileage !== undefined) v.mileage = req.mileage;
  if (req.year !== undefined) v.year = req.year;
  if (req.make !== undefined) v.make = req.make;
  if (req.model !== undefined) v.model = req.model;
  if (req.trim !== undefined) v.trim = req.trim;
  return delay({ ...v });
}

export async function deleteVehicle(id: string): Promise<void> {
  const idx = store.findIndex((x) => x.id === id);
  if (idx === -1) return delayReject(new Error('Vehicle not found'));
  store.splice(idx, 1);
  return delay(undefined);
}

// Internal helper used by the service-log mock to advance odometer on logging.
export function getVehicleSync(id: string): Vehicle | undefined {
  return store.find((x) => x.id === id);
}

// ── VIN lookup (simulated NHTSA vPIC round-trip) ─────────────────────────────
// A few known VINs decode; anything else returns an empty (best-effort) result,
// matching the backend behavior where the user then fills in details manually.
const VIN_DB: Record<string, VinDecodeResult> = {
  '3TMCZ5AN6KM254187': { year: 2019, make: 'Toyota', model: 'Tacoma', trim: 'TRD Off-Road' },
  '19XFC1F39ME201145': { year: 2021, make: 'Honda', model: 'Civic', trim: 'EX' },
  '4S4BSENC2F3261904': { year: 2015, make: 'Subaru', model: 'Outback', trim: '3.6R Limited' },
  '1FTFW1ET5DFC10312': { year: 2013, make: 'Ford', model: 'F-150', trim: 'XLT' },
  '5YJ3E1EA7KF317856': { year: 2019, make: 'Tesla', model: 'Model 3', trim: 'Standard Range Plus' },
  WBA3A5C58DF123456: { year: 2013, make: 'BMW', model: '328i', trim: 'Sedan' },
};

export async function decodeVin(vin: string): Promise<VinDecodeResult> {
  const key = vin.trim().toUpperCase();
  const hit = VIN_DB[key];
  // NHTSA is slower than our CRUD — simulate a longer round-trip.
  return delay(hit ?? { year: null, make: null, model: null, trim: null }, 500, 900);
}
