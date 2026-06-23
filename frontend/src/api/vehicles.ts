import { apiFetch } from './api';
import type {
  CreateVehicleRequest,
  UpdateVehicleRequest,
  Vehicle,
  VinDecodeResult,
} from '../types';

export async function listVehicles(): Promise<Vehicle[]> {
  return apiFetch<Vehicle[]>('/vehicles');
}

export async function getVehicle(id: string): Promise<Vehicle> {
  return apiFetch<Vehicle>(`/vehicles/${id}`);
}

export async function createVehicle(req: CreateVehicleRequest): Promise<Vehicle> {
  return apiFetch<Vehicle>('/vehicles', { method: 'POST', body: JSON.stringify(req) });
}

export async function updateVehicle(id: string, req: UpdateVehicleRequest): Promise<Vehicle> {
  return apiFetch<Vehicle>(`/vehicles/${id}`, { method: 'PATCH', body: JSON.stringify(req) });
}

export async function deleteVehicle(id: string): Promise<void> {
  return apiFetch<void>(`/vehicles/${id}`, { method: 'DELETE' });
}

export async function decodeVin(vin: string): Promise<VinDecodeResult> {
  return apiFetch<VinDecodeResult>(`/vin/${encodeURIComponent(vin.trim())}`);
}
