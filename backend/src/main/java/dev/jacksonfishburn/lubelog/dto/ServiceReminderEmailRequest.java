package dev.jacksonfishburn.lubelog.dto;

public record ServiceReminderEmailRequest(
        String toEmail,
        String serviceTypeName,
        VehicleInfo vehicleInfo,
        Integer currentMileage,
        Integer dueMileage) {
}
