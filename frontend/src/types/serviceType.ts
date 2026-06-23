// Matches dto.ServiceTypeResponse / ServiceTypeRequest.
// The backend entity is `ServiceType` (table `services`); a null user_id means global.

export interface ServiceType {
  id: string;
  name: string;
  isGlobal: boolean;
  createdAt: string; // ISO-8601 instant
}

// POST /api/service-types body (custom types only).
export interface CreateServiceTypeRequest {
  name: string;
}
