package dev.jacksonfishburn.lubelog.dto;

import java.time.Instant;
import java.util.UUID;

public record VehicleResponse(
        UUID id,
        UUID userId,
        Short year,
        String make,
        String model,
        String trim,
        String vin,
        String nickname,
        Integer mileage,
        Instant createdAt) {
}
