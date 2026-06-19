package dev.jacksonfishburn.lubelog.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import dev.jacksonfishburn.lubelog.entity.ServiceType;
import org.springframework.stereotype.Service;

import dev.jacksonfishburn.lubelog.dto.ServiceTypeRequest;
import dev.jacksonfishburn.lubelog.dto.ServiceTypeResponse;
import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.exception.AccessDeniedException;
import dev.jacksonfishburn.lubelog.exception.CannotDeleteGlobalServiceTypeException;
import dev.jacksonfishburn.lubelog.exception.DuplicateServiceTypeException;
import dev.jacksonfishburn.lubelog.exception.ResourceNotFoundException;
import dev.jacksonfishburn.lubelog.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceTypeService {

    private final ServiceRepository serviceRepository;

    public List<ServiceTypeResponse> getServiceTypes(User currentUser) {
        return Stream.concat(
                        serviceRepository.findAllByIsGlobalTrue().stream(),
                        serviceRepository.findAllByUserId(currentUser.getId()).stream())
                .map(this::toResponse)
                .toList();
    }

    public ServiceTypeResponse createServiceType(User currentUser, ServiceTypeRequest request) {
        String name = request.name();

        if (serviceRepository.existsByIsGlobalTrueAndNameIgnoreCase(name)
                || serviceRepository.existsByUserIdAndNameIgnoreCase(currentUser.getId(), name)) {
            throw new DuplicateServiceTypeException(name);
        }

        ServiceType serviceType = ServiceType.builder()
                .user(currentUser)
                .name(name)
                .isGlobal(false)
                .build();

        serviceType = serviceRepository.save(serviceType);
        return toResponse(serviceType);
    }

    public void deleteServiceType(User currentUser, UUID id) {
        ServiceType serviceType = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceType", id));

        if (serviceType.isGlobal()) {
            throw new CannotDeleteGlobalServiceTypeException();
        }

        if (!serviceType.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException();
        }

        serviceRepository.delete(serviceType);
    }

    private ServiceTypeResponse toResponse(ServiceType serviceType) {
        return new ServiceTypeResponse(
                serviceType.getId(),
                serviceType.getName(),
                serviceType.isGlobal(),
                serviceType.getCreatedAt());
    }
}
