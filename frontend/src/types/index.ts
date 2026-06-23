// Barrel for the DTO-mirroring types. These are shared by the mock layer and,
// later, the real fetch layer — neither changes when the data source swaps.
export type { User } from './user';
export type {
  Vehicle,
  CreateVehicleRequest,
  UpdateVehicleRequest,
  VinDecodeResult,
} from './vehicle';
export type { ServiceType, CreateServiceTypeRequest } from './serviceType';
export type { VehicleService, VehicleServiceRequest } from './vehicleService';
export type {
  ServiceLog,
  LogDetail,
  CreateLogRequest,
  LogDetailRequest,
} from './serviceLog';
