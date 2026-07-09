package dev.jacksonfishburn.lubelog.dto.vehicle;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record VehicleUpdateRequest(
        @Size(max = 100) String nickname,
        @PositiveOrZero Integer mileage,
        @Min(1900) @Max(2100) Integer year,
        @Size(max = 100) String make,
        @Size(max = 100) String model,
        @Size(max = 100) String trim) {
}
