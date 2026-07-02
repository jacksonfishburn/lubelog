// ── MOCK DATA LAYER: vehicle services (per-vehicle interval config) ──────────
// Mirrors /api/vehicles/{vehicleId}/services (GET list, GET one, POST, PUT, DELETE).
// Intervals live here, not on the service type (key design decision).
//
// TO GO REAL: fetch against `${API_BASE}/vehicles/${vehicleId}/services`.
import type { VehicleService, VehicleServiceRequest } from '../types';
import { delay, delayReject } from '../lib/latency';
import { newId } from '../lib/id';
import { SERVICE_TYPE_IDS } from './servicetypes';
import { VEHICLE_IDS } from './vehicles';

// Lookup so seeds can carry the denormalized serviceTypeName (as the API does).
const TYPE_NAMES: Record<string, string> = {
  [SERVICE_TYPE_IDS.oilChange]: 'Oil Change',
  [SERVICE_TYPE_IDS.tireRotation]: 'Tire Rotation',
  [SERVICE_TYPE_IDS.airFilter]: 'Air Filter',
  [SERVICE_TYPE_IDS.cabinAirFilter]: 'Cabin Air Filter',
  [SERVICE_TYPE_IDS.transmission]: 'Transmission Service',
  [SERVICE_TYPE_IDS.brakeInspection]: 'Brake Inspection',
  [SERVICE_TYPE_IDS.coolantFlush]: 'Coolant Flush',
  [SERVICE_TYPE_IDS.sparkPlugs]: 'Spark Plugs',
  [SERVICE_TYPE_IDS.differential]: 'Differential Service',
  [SERVICE_TYPE_IDS.fuelFilter]: 'Fuel Filter',
  [SERVICE_TYPE_IDS.wiperBlades]: 'Wiper Blades',
  [SERVICE_TYPE_IDS.batteryReplacement]: 'Battery Replacement',
};

export const VEHICLE_SERVICE_IDS = {
  tacomaOil: '40000000-0000-0000-0000-000000000001',
  tacomaTires: '40000000-0000-0000-0000-000000000002',
  tacomaBrakes: '40000000-0000-0000-0000-000000000003',
  tacomaCabin: '40000000-0000-0000-0000-000000000004',
  civicOil: '40000000-0000-0000-0000-000000000005',
  civicTires: '40000000-0000-0000-0000-000000000006',
  civicTransmission: '40000000-0000-0000-0000-000000000007',
  outbackOil: '40000000-0000-0000-0000-000000000008',
  outbackPlugs: '40000000-0000-0000-0000-000000000009',
  outbackBattery: '40000000-0000-0000-0000-00000000000a',
} as const;

const SEED: VehicleService[] = [
  // Work Truck (Tacoma) ─ 78,000 mi
  { id: VEHICLE_SERVICE_IDS.tacomaOil, vehicleId: VEHICLE_IDS.tacoma, serviceTypeId: SERVICE_TYPE_IDS.oilChange, serviceTypeName: 'Oil Change', intervalMiles: 5000, intervalMonths: 6, remindWhenDue: false, createdAt: '2026-01-05T14:20:00Z' },
  { id: VEHICLE_SERVICE_IDS.tacomaTires, vehicleId: VEHICLE_IDS.tacoma, serviceTypeId: SERVICE_TYPE_IDS.tireRotation, serviceTypeName: 'Tire Rotation', intervalMiles: 7500, intervalMonths: null, remindWhenDue: false, createdAt: '2026-01-05T14:21:00Z' },
  { id: VEHICLE_SERVICE_IDS.tacomaBrakes, vehicleId: VEHICLE_IDS.tacoma, serviceTypeId: SERVICE_TYPE_IDS.brakeInspection, serviceTypeName: 'Brake Inspection', intervalMiles: null, intervalMonths: 12, remindWhenDue: false, createdAt: '2026-01-05T14:22:00Z' },
  { id: VEHICLE_SERVICE_IDS.tacomaCabin, vehicleId: VEHICLE_IDS.tacoma, serviceTypeId: SERVICE_TYPE_IDS.cabinAirFilter, serviceTypeName: 'Cabin Air Filter', intervalMiles: 15000, intervalMonths: null, remindWhenDue: false, createdAt: '2026-01-05T14:23:00Z' },
  // Daily (Civic) ─ 41,200 mi
  { id: VEHICLE_SERVICE_IDS.civicOil, vehicleId: VEHICLE_IDS.civic, serviceTypeId: SERVICE_TYPE_IDS.oilChange, serviceTypeName: 'Oil Change', intervalMiles: 7500, intervalMonths: 12, remindWhenDue: false, createdAt: '2026-01-06T10:00:00Z' },
  { id: VEHICLE_SERVICE_IDS.civicTires, vehicleId: VEHICLE_IDS.civic, serviceTypeId: SERVICE_TYPE_IDS.tireRotation, serviceTypeName: 'Tire Rotation', intervalMiles: 6000, intervalMonths: null, remindWhenDue: false, createdAt: '2026-01-06T10:01:00Z' },
  { id: VEHICLE_SERVICE_IDS.civicTransmission, vehicleId: VEHICLE_IDS.civic, serviceTypeId: SERVICE_TYPE_IDS.transmission, serviceTypeName: 'Transmission Service', intervalMiles: 60000, intervalMonths: null, remindWhenDue: false, createdAt: '2026-01-06T10:02:00Z' },
  // Adventure Rig (Outback) ─ 132,500 mi
  { id: VEHICLE_SERVICE_IDS.outbackOil, vehicleId: VEHICLE_IDS.outback, serviceTypeId: SERVICE_TYPE_IDS.oilChange, serviceTypeName: 'Oil Change', intervalMiles: 5000, intervalMonths: 6, remindWhenDue: false, createdAt: '2026-01-09T18:00:00Z' },
  { id: VEHICLE_SERVICE_IDS.outbackPlugs, vehicleId: VEHICLE_IDS.outback, serviceTypeId: SERVICE_TYPE_IDS.sparkPlugs, serviceTypeName: 'Spark Plugs', intervalMiles: 100000, intervalMonths: null, remindWhenDue: false, createdAt: '2026-01-09T18:01:00Z' },
  { id: VEHICLE_SERVICE_IDS.outbackBattery, vehicleId: VEHICLE_IDS.outback, serviceTypeId: SERVICE_TYPE_IDS.batteryReplacement, serviceTypeName: 'Battery Replacement', intervalMiles: null, intervalMonths: 48, remindWhenDue: false, createdAt: '2026-01-09T18:02:00Z' },
];

const store: VehicleService[] = [...SEED];

export async function listVehicleServices(vehicleId: string): Promise<VehicleService[]> {
  return delay(store.filter((vs) => vs.vehicleId === vehicleId).map((vs) => ({ ...vs })));
}

export async function getVehicleService(vehicleId: string, vsId: string): Promise<VehicleService> {
  const vs = store.find((x) => x.id === vsId && x.vehicleId === vehicleId);
  if (!vs) return delayReject(new Error('Vehicle service not found'));
  return delay({ ...vs });
}

export async function createVehicleService(
  vehicleId: string,
  req: VehicleServiceRequest,
): Promise<VehicleService> {
  if (req.intervalMiles == null && req.intervalMonths == null) {
    return delayReject(new Error('Set at least one interval (miles or months)'));
  }
  // Backend unique (vehicle_id, service_id).
  const dup = store.some((vs) => vs.vehicleId === vehicleId && vs.serviceTypeId === req.serviceTypeId);
  if (dup) return delayReject(new Error('That service is already configured on this vehicle'));

  const created: VehicleService = {
    id: newId(),
    vehicleId,
    serviceTypeId: req.serviceTypeId,
    serviceTypeName: TYPE_NAMES[req.serviceTypeId] ?? 'Service',
    intervalMiles: req.intervalMiles ?? null,
    intervalMonths: req.intervalMonths ?? null,
    remindWhenDue: req.remindWhenDue ?? false,
    createdAt: new Date().toISOString(),
  };
  store.push(created);
  return delay({ ...created });
}

export async function updateVehicleService(
  vehicleId: string,
  vsId: string,
  req: VehicleServiceRequest,
): Promise<VehicleService> {
  const vs = store.find((x) => x.id === vsId && x.vehicleId === vehicleId);
  if (!vs) return delayReject(new Error('Vehicle service not found'));
  if (req.intervalMiles == null && req.intervalMonths == null) {
    return delayReject(new Error('Set at least one interval (miles or months)'));
  }
  vs.serviceTypeId = req.serviceTypeId;
  vs.serviceTypeName = TYPE_NAMES[req.serviceTypeId] ?? vs.serviceTypeName;
  vs.intervalMiles = req.intervalMiles ?? null;
  vs.intervalMonths = req.intervalMonths ?? null;
  vs.remindWhenDue = req.remindWhenDue ?? false;
  return delay({ ...vs });
}

export async function deleteVehicleService(vehicleId: string, vsId: string): Promise<void> {
  const idx = store.findIndex((x) => x.id === vsId && x.vehicleId === vehicleId);
  if (idx === -1) return delayReject(new Error('Vehicle service not found'));
  store.splice(idx, 1);
  return delay(undefined);
}

// Internal: lets the service-log mock map a vehicleServiceId back to its vehicle.
export function vehicleServicesForVehicleSync(vehicleId: string): VehicleService[] {
  return store.filter((vs) => vs.vehicleId === vehicleId);
}
export function getVehicleServiceSync(vsId: string): VehicleService | undefined {
  return store.find((vs) => vs.id === vsId);
}
