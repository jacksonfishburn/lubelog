package dev.jacksonfishburn.lubelog.dto;

public record VinDecodeResponse(
        Short year,
        String make,
        String model,
        String trim) {
}
