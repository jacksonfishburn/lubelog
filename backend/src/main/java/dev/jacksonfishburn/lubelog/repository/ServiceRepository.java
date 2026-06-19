package dev.jacksonfishburn.lubelog.repository;

import dev.jacksonfishburn.lubelog.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<ServiceType, UUID> {
    List<ServiceType> findAllByUserId(UUID userId);
    List<ServiceType> findAllByIsGlobalTrue();
    boolean existsByUserIdAndNameIgnoreCase(UUID userId, String name);
    boolean existsByIsGlobalTrueAndNameIgnoreCase(String name);
}

