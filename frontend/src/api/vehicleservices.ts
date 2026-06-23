import { apiFetch } from './api';
import type { VehicleService, VehicleServiceRequest } from '../types';

export async function listVehicleServices(vehicleId: string): Promise<VehicleService[]> {
  return apiFetch<VehicleService[]>(`/vehicles/${vehicleId}/services`);
}

export async function getVehicleService(vehicleId: string, vsId: string): Promise<VehicleService> {
  return apiFetch<VehicleService>(`/vehicles/${vehicleId}/services/${vsId}`);
}

export async function createVehicleService(
  vehicleId: string,
  req: VehicleServiceRequest,
): Promise<VehicleService> {
  return apiFetch<VehicleService>(`/vehicles/${vehicleId}/services`, {
    method: 'POST',
    body: JSON.stringify(req),
  });
}

export async function updateVehicleService(
  vehicleId: string,
  vsId: string,
  req: VehicleServiceRequest,
): Promise<VehicleService> {
  return apiFetch<VehicleService>(`/vehicles/${vehicleId}/services/${vsId}`, {
    method: 'PUT',
    body: JSON.stringify(req),
  });
}

export async function deleteVehicleService(vehicleId: string, vsId: string): Promise<void> {
  return apiFetch<void>(`/vehicles/${vehicleId}/services/${vsId}`, { method: 'DELETE' });
}
