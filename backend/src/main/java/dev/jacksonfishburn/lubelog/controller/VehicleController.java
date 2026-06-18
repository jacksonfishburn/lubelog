package dev.jacksonfishburn.lubelog.controller;

import java.util.List;
import java.util.UUID;

import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.security.AuthUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jacksonfishburn.lubelog.dto.VehicleRequest;
import dev.jacksonfishburn.lubelog.dto.VehicleResponse;
import dev.jacksonfishburn.lubelog.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest request) {
        User user = authUtils.getCurrentUser();
        VehicleResponse response = vehicleService.createVehicle(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getVehicle(@PathVariable UUID id) {
        User user = authUtils.getCurrentUser();
        return ResponseEntity.ok(vehicleService.getVehicle(user, id));
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getVehicles() {
        User user = authUtils.getCurrentUser();
        return ResponseEntity.ok(vehicleService.getAllVehicles(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable UUID id) {
        User user = authUtils.getCurrentUser();
        vehicleService.deleteVehicle(user, id);
        return ResponseEntity.noContent().build();
    }
}
