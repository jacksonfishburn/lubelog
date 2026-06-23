// Matches dto.VehicleServiceResponse / VehicleServiceRequest.
// Per the key design decision: intervals live HERE, not on the service type.
// At least one of intervalMiles / intervalMonths must be set (backend check constraint).

export interface VehicleService {
  id: string;
  vehicleId: string;
  serviceTypeId: string;
  serviceTypeName: string; // denormalized for display
  intervalMiles: number | null;
  intervalMonths: number | null;
  createdAt: string; // ISO-8601 instant
}

// Body for POST/PUT /api/vehicles/{vehicleId}/services[/{vsId}].
export interface VehicleServiceRequest {
  serviceTypeId: string;
  intervalMiles?: number | null;
  intervalMonths?: number | null;
}
