// Matches dto.LogResponse / LogRequest / LogDetailResponse / LogDetailRequest.
// `mileageDue` and `dateDue` are COMPUTED by the backend on read (not stored):
//   mileageDue = doneAtMileage + intervalMiles   (null if either is null)
//   dateDue    = doneAtDate + intervalMonths      (null if intervalMonths is null)

export interface LogDetail {
  id: string;
  key: string;
  value: string;
}

export interface ServiceLog {
  id: string;
  vehicleServiceId: string;
  doneAtMileage: number | null;
  mileageDue: number | null; // computed
  doneAtDate: string; // LocalDate "YYYY-MM-DD"
  dateDue: string | null; // computed, "YYYY-MM-DD"
  cost: number | null;
  notes: string | null;
  details: LogDetail[];
}

// POST/PUT /api/logs body.
export interface CreateLogRequest {
  vehicleServiceId: string;
  doneAtMileage?: number | null;
  doneAtDate: string; // required
  cost?: number | null;
  notes?: string | null;
  details?: LogDetailRequest[];
}

// POST /api/logs/{logId}/details body.
export interface LogDetailRequest {
  key: string;
  value: string;
}
