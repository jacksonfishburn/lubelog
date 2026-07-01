package dev.jacksonfishburn.lubelog.repository;

import dev.jacksonfishburn.lubelog.entity.ServiceReminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServiceReminderRepository extends JpaRepository<ServiceReminder, UUID> {
    Optional<ServiceReminder> findByVehicleServiceId(UUID vehicleServiceId);
}
