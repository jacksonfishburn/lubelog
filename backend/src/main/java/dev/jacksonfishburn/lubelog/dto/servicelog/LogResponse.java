package dev.jacksonfishburn.lubelog.dto.servicelog;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record LogResponse(
        UUID id,
        UUID vehicleServiceId,
        Integer doneAtMileage,
        Integer mileageDue,
        LocalDate doneAtDate,
        LocalDate dateDue,
        BigDecimal cost,
        String notes,
        List<LogDetailResponse> details) {
}
