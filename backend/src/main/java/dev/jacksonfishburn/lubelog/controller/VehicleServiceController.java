package dev.jacksonfishburn.lubelog.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jacksonfishburn.lubelog.dto.VehicleServiceRequest;
import dev.jacksonfishburn.lubelog.dto.VehicleServiceResponse;
import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.security.AuthUtils;
import dev.jacksonfishburn.lubelog.service.VehicleServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vehicles/{vehicleId}/services")
@RequiredArgsConstructor
public class VehicleServiceController {

    private final VehicleServiceService vehicleServiceService;
    private final AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<List<VehicleServiceResponse>> getVehicleServices(@PathVariable UUID vehicleId) {
        User user = authUtils.getCurrentUser();
        return ResponseEntity.ok(vehicleServiceService.getVehicleServices(user, vehicleId));
    }

    @PostMapping
    public ResponseEntity<VehicleServiceResponse> createVehicleService(
            @PathVariable UUID vehicleId, @Valid @RequestBody VehicleServiceRequest request) {
        User user = authUtils.getCurrentUser();
        VehicleServiceResponse response = vehicleServiceService.createVehicleService(user, vehicleId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{vsId}")
    public ResponseEntity<VehicleServiceResponse> getVehicleService(@PathVariable UUID vehicleId, @PathVariable UUID vsId) {
        User user = authUtils.getCurrentUser();
        return ResponseEntity.ok(vehicleServiceService.getVehicleService(user, vehicleId, vsId));
    }

    @PutMapping("/{vsId}")
    public ResponseEntity<VehicleServiceResponse> updateVehicleService(
            @PathVariable UUID vehicleId, @PathVariable UUID vsId, @Valid @RequestBody VehicleServiceRequest request) {
        User user = authUtils.getCurrentUser();
        return ResponseEntity.ok(vehicleServiceService.updateVehicleService(user, vehicleId, vsId, request));
    }

    @DeleteMapping("/{vsId}")
    public ResponseEntity<Void> deleteVehicleService(@PathVariable UUID vehicleId, @PathVariable UUID vsId) {
        User user = authUtils.getCurrentUser();
        vehicleServiceService.deleteVehicleService(user, vehicleId, vsId);
        return ResponseEntity.noContent().build();
    }
}
