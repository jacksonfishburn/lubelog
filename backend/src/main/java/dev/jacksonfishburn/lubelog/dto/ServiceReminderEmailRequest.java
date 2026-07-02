package dev.jacksonfishburn.lubelog.dto;

import java.time.LocalDate;

public record ServiceReminderEmailRequest(
        String toEmail,
        String serviceTypeName,
        VehicleInfo vehicleInfo,
        Integer currentMileage,
        Integer dueMileage,
        LocalDate dueDate) {
}
