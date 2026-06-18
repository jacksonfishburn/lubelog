package dev.jacksonfishburn.lubelog.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.jacksonfishburn.lubelog.dto.VehicleRequest;
import dev.jacksonfishburn.lubelog.dto.VehicleResponse;
import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.entity.Vehicle;
import dev.jacksonfishburn.lubelog.exception.AccessDeniedException;
import dev.jacksonfishburn.lubelog.exception.ResourceNotFoundException;
import dev.jacksonfishburn.lubelog.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleResponse createVehicle(User currentUser, VehicleRequest request) {
        Vehicle vehicle = Vehicle.builder()
                .user(currentUser)
                .year(request.year())
                .make(request.make())
                .model(request.model())
                .trim(request.trim())
                .vin(request.vin())
                .nickname(request.nickname())
                .mileage(request.mileage())
                .build();

        vehicle = vehicleRepository.save(vehicle);
        return toResponse(vehicle);
    }

    public VehicleResponse getVehicle(User currentUser, UUID id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));

        if (!vehicle.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException();
        }

        return toResponse(vehicle);
    }

    public List<VehicleResponse> getAllVehicles(User currentUser) {
        return vehicleRepository.findAllByUserId(currentUser.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteVehicle(User currentUser, UUID id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));

        if (!vehicle.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException();
        }

        vehicleRepository.delete(vehicle);
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getUser().getId(),
                vehicle.getYear(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getTrim(),
                vehicle.getVin(),
                vehicle.getNickname(),
                vehicle.getMileage(),
                vehicle.getCreatedAt());
    }
}
