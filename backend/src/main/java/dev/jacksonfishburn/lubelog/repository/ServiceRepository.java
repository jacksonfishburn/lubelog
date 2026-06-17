package dev.jacksonfishburn.lubelog.repository;

import dev.jacksonfishburn.lubelog.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<Service, UUID> {
    List<Service> findAllByUserId(UUID userId);
    List<Service> findAllByIsGlobalTrue();
}

