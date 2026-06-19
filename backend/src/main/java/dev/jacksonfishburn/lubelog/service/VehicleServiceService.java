package dev.jacksonfishburn.lubelog.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.jacksonfishburn.lubelog.dto.VehicleServiceRequest;
import dev.jacksonfishburn.lubelog.dto.VehicleServiceResponse;
import dev.jacksonfishburn.lubelog.entity.ServiceType;
import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.entity.Vehicle;
import dev.jacksonfishburn.lubelog.entity.VehicleService;
import dev.jacksonfishburn.lubelog.exception.AccessDeniedException;
import dev.jacksonfishburn.lubelog.exception.DuplicateVehicleServiceException;
import dev.jacksonfishburn.lubelog.exception.InvalidServiceIntervalException;
import dev.jacksonfishburn.lubelog.exception.ResourceNotFoundException;
import dev.jacksonfishburn.lubelog.repository.ServiceRepository;
import dev.jacksonfishburn.lubelog.repository.VehicleRepository;
import dev.jacksonfishburn.lubelog.repository.VehicleServiceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleServiceService {

    private final VehicleServiceRepository vehicleServiceRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceRepository serviceRepository;

    public List<VehicleServiceResponse> getVehicleServices(User currentUser, UUID vehicleId) {
        Vehicle vehicle = getOwnedVehicle(currentUser, vehicleId);
        return vehicleServiceRepository.findAllByVehicleId(vehicle.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public VehicleServiceResponse createVehicleService(User currentUser, UUID vehicleId, VehicleServiceRequest request) {
        Vehicle vehicle = getOwnedVehicle(currentUser, vehicleId);
        requireValidInterval(request);
        ServiceType serviceType = getAccessibleServiceType(currentUser, request.serviceTypeId());

        if (vehicleServiceRepository.findByVehicleIdAndServiceId(vehicle.getId(), serviceType.getId()).isPresent()) {
            throw new DuplicateVehicleServiceException(serviceType.getName());
        }

        VehicleService vehicleService = VehicleService.builder()
                .vehicle(vehicle)
                .service(serviceType)
                .intervalMiles(request.intervalMiles())
                .intervalMonths(toShort(request.intervalMonths()))
                .build();

        vehicleService = vehicleServiceRepository.save(vehicleService);
        return toResponse(vehicleService);
    }

    public VehicleServiceResponse getVehicleService(User currentUser, UUID vehicleId, UUID vsId) {
        VehicleService vehicleService = getOwnedVehicleService(currentUser, vehicleId, vsId);
        return toResponse(vehicleService);
    }

    public VehicleServiceResponse updateVehicleService(User currentUser, UUID vehicleId, UUID vsId, VehicleServiceRequest request) {
        VehicleService vehicleService = getOwnedVehicleService(currentUser, vehicleId, vsId);
        requireValidInterval(request);
        ServiceType serviceType = getAccessibleServiceType(currentUser, request.serviceTypeId());

        if (!serviceType.getId().equals(vehicleService.getService().getId())
                && vehicleServiceRepository.findByVehicleIdAndServiceId(vehicleService.getVehicle().getId(), serviceType.getId()).isPresent()) {
            throw new DuplicateVehicleServiceException(serviceType.getName());
        }

        vehicleService.setService(serviceType);
        vehicleService.setIntervalMiles(request.intervalMiles());
        vehicleService.setIntervalMonths(toShort(request.intervalMonths()));

        vehicleService = vehicleServiceRepository.save(vehicleService);
        return toResponse(vehicleService);
    }

    public void deleteVehicleService(User currentUser, UUID vehicleId, UUID vsId) {
        VehicleService vehicleService = getOwnedVehicleService(currentUser, vehicleId, vsId);
        vehicleServiceRepository.delete(vehicleService);
    }

    private void requireValidInterval(VehicleServiceRequest request) {
        if (request.intervalMiles() == null && request.intervalMonths() == null) {
            throw new InvalidServiceIntervalException();
        }
    }

    private ServiceType getAccessibleServiceType(User currentUser, UUID serviceTypeId) {
        ServiceType serviceType = serviceRepository.findById(serviceTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceType", serviceTypeId));

        boolean accessible = serviceType.isGlobal()
                || (serviceType.getUser() != null && serviceType.getUser().getId().equals(currentUser.getId()));
        if (!accessible) {
            throw new AccessDeniedException();
        }

        return serviceType;
    }

    private Vehicle getOwnedVehicle(User currentUser, UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicleId));

        if (!vehicle.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException();
        }

        return vehicle;
    }

    private VehicleService getOwnedVehicleService(User currentUser, UUID vehicleId, UUID vsId) {
        VehicleService vehicleService = vehicleServiceRepository.findById(vsId)
                .orElseThrow(() -> new ResourceNotFoundException("VehicleService", vsId));

        if (!vehicleService.getVehicle().getId().equals(vehicleId)) {
            throw new ResourceNotFoundException("VehicleService", vsId);
        }

        if (!vehicleService.getVehicle().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException();
        }

        return vehicleService;
    }

    private Short toShort(Integer value) {
        return value != null ? value.shortValue() : null;
    }

    private VehicleServiceResponse toResponse(VehicleService vehicleService) {
        Integer intervalMonths = vehicleService.getIntervalMonths() != null
                ? vehicleService.getIntervalMonths().intValue()
                : null;

        return new VehicleServiceResponse(
                vehicleService.getId(),
                vehicleService.getVehicle().getId(),
                vehicleService.getService().getId(),
                vehicleService.getService().getName(),
                vehicleService.getIntervalMiles(),
                intervalMonths,
                vehicleService.getCreatedAt());
    }
}
