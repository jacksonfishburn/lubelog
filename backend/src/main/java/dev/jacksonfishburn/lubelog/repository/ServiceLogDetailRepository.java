package dev.jacksonfishburn.lubelog.repository;

import dev.jacksonfishburn.lubelog.entity.ServiceLogDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServiceLogDetailRepository extends JpaRepository<ServiceLogDetail, UUID> {
    List<ServiceLogDetail> findAllByServiceLogId(UUID serviceLogId);
}

