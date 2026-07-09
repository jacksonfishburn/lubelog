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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jacksonfishburn.lubelog.dto.servicelog.LogDetailRequest;
import dev.jacksonfishburn.lubelog.dto.servicelog.LogDetailResponse;
import dev.jacksonfishburn.lubelog.dto.servicelog.LogRequest;
import dev.jacksonfishburn.lubelog.dto.servicelog.LogResponse;
import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.exception.InvalidRequestException;
import dev.jacksonfishburn.lubelog.security.AuthUtils;
import dev.jacksonfishburn.lubelog.service.ServiceLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class ServiceLogController {

    private final ServiceLogService serviceLogService;
    private final AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<List<LogResponse>> getLogs(
            @RequestParam(required = false) UUID vehicleServiceId,
            @RequestParam(required = false) UUID vehicleId) {
        User user = authUtils.getCurrentUser();

        if (vehicleServiceId != null) {
            return ResponseEntity.ok(serviceLogService.getLogsByVehicleService(user, vehicleServiceId));
        }
        if (vehicleId != null) {
            return ResponseEntity.ok(serviceLogService.getLogsByVehicle(user, vehicleId));
        }
        throw new InvalidRequestException("Either vehicleServiceId or vehicleId must be provided");
    }

    @PostMapping
    public ResponseEntity<LogResponse> createLog(@Valid @RequestBody LogRequest request) {
        User user = authUtils.getCurrentUser();
        LogResponse response = serviceLogService.createLog(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{logId}")
    public ResponseEntity<LogResponse> getLog(@PathVariable UUID logId) {
        User user = authUtils.getCurrentUser();
        return ResponseEntity.ok(serviceLogService.getLog(user, logId));
    }

    @PutMapping("/{logId}")
    public ResponseEntity<LogResponse> updateLog(@PathVariable UUID logId, @Valid @RequestBody LogRequest request) {
        User user = authUtils.getCurrentUser();
        return ResponseEntity.ok(serviceLogService.updateLog(user, logId, request));
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteLog(@PathVariable UUID logId) {
        User user = authUtils.getCurrentUser();
        serviceLogService.deleteLog(user, logId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{logId}/details")
    public ResponseEntity<LogDetailResponse> addDetail(@PathVariable UUID logId, @Valid @RequestBody LogDetailRequest request) {
        User user = authUtils.getCurrentUser();
        LogDetailResponse response = serviceLogService.addDetail(user, logId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/details/{detailId}")
    public ResponseEntity<Void> deleteDetail(@PathVariable UUID detailId) {
        User user = authUtils.getCurrentUser();
        serviceLogService.deleteDetail(user, detailId);
        return ResponseEntity.noContent().build();
    }
}
