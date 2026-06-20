package dev.jacksonfishburn.lubelog.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record LogRequest(
        @NotNull UUID vehicleServiceId,
        Integer doneAtMileage,
        @NotNull LocalDate doneAtDate,
        BigDecimal cost,
        String notes,
        @Valid List<LogDetailRequest> details) {
}
