package dev.jacksonfishburn.lubelog.dto.vin;

public record VinDecodeResponse(
        Short year,
        String make,
        String model,
        String trim) {
}
