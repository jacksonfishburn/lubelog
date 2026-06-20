package dev.jacksonfishburn.lubelog.repository;

import dev.jacksonfishburn.lubelog.entity.ServiceLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServiceLogRepository extends JpaRepository<ServiceLog, UUID> {
    List<ServiceLog> findAllByVehicleServiceId(UUID vehicleServiceId);
    List<ServiceLog> findAllByVehicleService_Vehicle_Id(UUID vehicleId);
}

