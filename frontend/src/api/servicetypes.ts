import { apiFetch } from './api';
import type { CreateServiceTypeRequest, ServiceType } from '../types';

export async function listServiceTypes(): Promise<ServiceType[]> {
  return apiFetch<ServiceType[]>('/service-types');
}

export async function createServiceType(req: CreateServiceTypeRequest): Promise<ServiceType> {
  return apiFetch<ServiceType>('/service-types', { method: 'POST', body: JSON.stringify(req) });
}

export async function deleteServiceType(id: string): Promise<void> {
  return apiFetch<void>(`/service-types/${id}`, { method: 'DELETE' });
}
