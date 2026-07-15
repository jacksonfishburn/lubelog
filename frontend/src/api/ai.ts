import { apiFetch } from './api';
import type { AiFindPartsResponse } from '../types';

export async function findParts(
  vehicleId: string,
  serviceTypeId: string,
): Promise<AiFindPartsResponse> {
  return apiFetch<AiFindPartsResponse>(
    `/ai/find-parts/vehicles/${vehicleId}/service-types/${serviceTypeId}`,
  );
}
