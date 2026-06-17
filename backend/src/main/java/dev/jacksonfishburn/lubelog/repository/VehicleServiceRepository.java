package dev.jacksonfishburn.lubelog.repository;

import dev.jacksonfishburn.lubelog.entity.VehicleService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleServiceRepository extends JpaRepository<VehicleService, UUID> {
    List<VehicleService> findAllByVehicleId(UUID vehicleId);
    Optional<VehicleService> findByVehicleIdAndServiceId(UUID vehicleId, UUID serviceId);
}

