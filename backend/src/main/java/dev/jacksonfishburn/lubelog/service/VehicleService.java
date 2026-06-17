package dev.jacksonfishburn.lubelog.service;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import dev.jacksonfishburn.lubelog.dto.VehicleRequest;
import dev.jacksonfishburn.lubelog.dto.VehicleResponse;
import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.entity.Vehicle;
import dev.jacksonfishburn.lubelog.exception.AccessDeniedException;
import dev.jacksonfishburn.lubelog.exception.ResourceNotFoundException;
import dev.jacksonfishburn.lubelog.repository.UserRepository;
import dev.jacksonfishburn.lubelog.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public VehicleResponse createVehicle(VehicleRequest request) {
        User currentUser = getCurrentUser();

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

    public VehicleResponse getVehicle(UUID id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));

        User currentUser = getCurrentUser();
        if (!vehicle.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException();
        }

        return toResponse(vehicle);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            String keycloakId = jwtAuthentication.getToken().getSubject();
            return userRepository.findByKeycloakId(keycloakId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", keycloakId));
        }
        throw new AccessDeniedException();
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
