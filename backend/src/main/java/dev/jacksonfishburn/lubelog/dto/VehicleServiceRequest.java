package dev.jacksonfishburn.lubelog.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record VehicleServiceRequest(
        @NotNull UUID serviceTypeId,
        Integer intervalMiles,
        Integer intervalMonths,
        Boolean remindWhenDue) {
}
