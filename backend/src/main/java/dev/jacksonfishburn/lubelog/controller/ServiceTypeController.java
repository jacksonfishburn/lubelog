package dev.jacksonfishburn.lubelog.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jacksonfishburn.lubelog.dto.ServiceTypeRequest;
import dev.jacksonfishburn.lubelog.dto.ServiceTypeResponse;
import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.security.AuthUtils;
import dev.jacksonfishburn.lubelog.service.ServiceTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/service-types")
@RequiredArgsConstructor
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;
    private final AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<List<ServiceTypeResponse>> getServiceTypes() {
        User user = authUtils.getCurrentUser();
        return ResponseEntity.ok(serviceTypeService.getServiceTypes(user));
    }

    @PostMapping
    public ResponseEntity<ServiceTypeResponse> createServiceType(@Valid @RequestBody ServiceTypeRequest request) {
        User user = authUtils.getCurrentUser();
        ServiceTypeResponse response = serviceTypeService.createServiceType(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServiceType(@PathVariable UUID id) {
        User user = authUtils.getCurrentUser();
        serviceTypeService.deleteServiceType(user, id);
        return ResponseEntity.noContent().build();
    }
}
