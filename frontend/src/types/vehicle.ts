// Matches dto.VehicleResponse / VehicleRequest / VehicleUpdateRequest / VinDecodeResponse.
// Field names and nullability match the Java records exactly (Jackson camelCase).

export interface Vehicle {
  id: string;
  userId: string;
  year: number | null; // Short on the backend
  make: string | null;
  model: string | null;
  trim: string | null;
  vin: string | null;
  nickname: string | null;
  mileage: number | null;
  createdAt: string; // ISO-8601 instant
}

// POST /api/vehicles body. `vin` only exists on create.
export interface CreateVehicleRequest {
  vin?: string | null;
  nickname?: string | null;
  mileage?: number | null;
  year?: number | null;
  make?: string | null;
  model?: string | null;
  trim?: string | null;
}

// PATCH /api/vehicles/{id} body. No `vin` field on the backend update record.
export interface UpdateVehicleRequest {
  nickname?: string | null;
  mileage?: number | null;
  year?: number | null;
  make?: string | null;
  model?: string | null;
  trim?: string | null;
}

// GET /api/vin/{vin} response (best-effort decode).
export interface VinDecodeResult {
  year: number | null;
  make: string | null;
  model: string | null;
  trim: string | null;
}
