package dev.jacksonfishburn.lubelog.dto.vehicleservice;

import java.time.Instant;
import java.util.UUID;

public record VehicleServiceResponse(
        UUID id,
        UUID vehicleId,
        UUID serviceTypeId,
        String serviceTypeName,
        Integer intervalMiles,
        Integer intervalMonths,
        boolean remindWhenDue,
        Instant createdAt) {
}
