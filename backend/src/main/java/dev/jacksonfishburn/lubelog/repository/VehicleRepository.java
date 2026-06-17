package dev.jacksonfishburn.lubelog.repository;

import dev.jacksonfishburn.lubelog.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    List<Vehicle> findAllByUserId(UUID userId);
}

