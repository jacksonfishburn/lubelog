package dev.jacksonfishburn.lubelog.dto.reminder;

import dev.jacksonfishburn.lubelog.dto.vehicle.VehicleInfo;

import java.time.LocalDate;

public record ServiceReminderEmailRequest(
        String toEmail,
        String serviceTypeName,
        VehicleInfo vehicleInfo,
        Integer currentMileage,
        Integer dueMileage,
        LocalDate dueDate) {
}
