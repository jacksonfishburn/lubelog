import { apiFetch } from './api';
import type { CreateLogRequest, LogDetail, LogDetailRequest, ServiceLog } from '../types';

export async function listLogsByVehicleService(vehicleServiceId: string): Promise<ServiceLog[]> {
  return apiFetch<ServiceLog[]>(`/logs?vehicleServiceId=${encodeURIComponent(vehicleServiceId)}`);
}

export async function listLogsByVehicle(vehicleId: string): Promise<ServiceLog[]> {
  return apiFetch<ServiceLog[]>(`/logs?vehicleId=${encodeURIComponent(vehicleId)}`);
}

export async function getLog(logId: string): Promise<ServiceLog> {
  return apiFetch<ServiceLog>(`/logs/${logId}`);
}

export async function createLog(req: CreateLogRequest): Promise<ServiceLog> {
  return apiFetch<ServiceLog>('/logs', { method: 'POST', body: JSON.stringify(req) });
}

export async function updateLog(logId: string, req: CreateLogRequest): Promise<ServiceLog> {
  return apiFetch<ServiceLog>(`/logs/${logId}`, { method: 'PUT', body: JSON.stringify(req) });
}

export async function deleteLog(logId: string): Promise<void> {
  return apiFetch<void>(`/logs/${logId}`, { method: 'DELETE' });
}

export async function addDetail(logId: string, req: LogDetailRequest): Promise<LogDetail> {
  return apiFetch<LogDetail>(`/logs/${logId}/details`, {
    method: 'POST',
    body: JSON.stringify(req),
  });
}

export async function deleteDetail(detailId: string): Promise<void> {
  return apiFetch<void>(`/logs/details/${detailId}`, { method: 'DELETE' });
}
