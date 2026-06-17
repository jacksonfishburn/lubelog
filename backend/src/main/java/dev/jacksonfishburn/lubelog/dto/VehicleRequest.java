package dev.jacksonfishburn.lubelog.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record VehicleRequest(
        @Size(min = 17, max = 17, message = "VIN must be exactly 17 characters") String vin,
        @Size(max = 100) String nickname,
        @PositiveOrZero Integer mileage,
        Short year,
        @Size(max = 100) String make,
        @Size(max = 100) String model,
        @Size(max = 100) String trim) {
}
