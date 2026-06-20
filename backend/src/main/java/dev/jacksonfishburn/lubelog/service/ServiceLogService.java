package dev.jacksonfishburn.lubelog.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.jacksonfishburn.lubelog.dto.LogDetailRequest;
import dev.jacksonfishburn.lubelog.dto.LogDetailResponse;
import dev.jacksonfishburn.lubelog.dto.LogRequest;
import dev.jacksonfishburn.lubelog.dto.LogResponse;
import dev.jacksonfishburn.lubelog.entity.ServiceLog;
import dev.jacksonfishburn.lubelog.entity.ServiceLogDetail;
import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.entity.Vehicle;
import dev.jacksonfishburn.lubelog.entity.VehicleService;
import dev.jacksonfishburn.lubelog.exception.AccessDeniedException;
import dev.jacksonfishburn.lubelog.exception.InvalidServiceLogMileageException;
import dev.jacksonfishburn.lubelog.exception.ResourceNotFoundException;
import dev.jacksonfishburn.lubelog.repository.ServiceLogDetailRepository;
import dev.jacksonfishburn.lubelog.repository.ServiceLogRepository;
import dev.jacksonfishburn.lubelog.repository.VehicleRepository;
import dev.jacksonfishburn.lubelog.repository.VehicleServiceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceLogService {

    private final ServiceLogRepository serviceLogRepository;
    private final ServiceLogDetailRepository serviceLogDetailRepository;
    private final VehicleServiceRepository vehicleServiceRepository;
    private final VehicleRepository vehicleRepository;

    public List<LogResponse> getLogsByVehicleService(User currentUser, UUID vehicleServiceId) {
        VehicleService vehicleService = validateVehicleServiceOwnership(currentUser, vehicleServiceId);
        return serviceLogRepository.findAllByVehicleServiceId(vehicleService.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<LogResponse> getLogsByVehicle(User currentUser, UUID vehicleId) {
        Vehicle vehicle = getOwnedVehicle(currentUser, vehicleId);
        return serviceLogRepository.findAllByVehicleService_Vehicle_Id(vehicle.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public LogResponse createLog(User currentUser, LogRequest request) {
        VehicleService vehicleService = validateVehicleServiceOwnership(currentUser, request.vehicleServiceId());
        Vehicle vehicle = vehicleService.getVehicle();

        Integer currentMileage = getVehicleMileage(vehicle);
        requireValidMileage(request.doneAtMileage(), currentMileage);

        ServiceLog log = ServiceLog.builder()
                .vehicleService(vehicleService)
                .doneAtMileage(request.doneAtMileage())
                .doneAtDate(request.doneAtDate())
                .cost(request.cost())
                .notes(request.notes())
                .build();
        log = serviceLogRepository.save(log);

        if (request.doneAtMileage() != null && (currentMileage == null || request.doneAtMileage() > currentMileage)) {
            setVehicleMileage(vehicle, request.doneAtMileage());
        }

        List<ServiceLogDetail> details = saveDetails(log, request.details());
        return toResponse(log, details);
    }

    public LogResponse getLog(User currentUser, UUID logId) {
        ServiceLog log = getOwnedLog(currentUser, logId);
        return toResponse(log);
    }

    public LogResponse updateLog(User currentUser, UUID logId, LogRequest request) {
        ServiceLog log = getOwnedLog(currentUser, logId);
        Vehicle vehicle = log.getVehicleService().getVehicle();

        Integer currentMileage = getVehicleMileage(vehicle);
        requireValidMileage(request.doneAtMileage(), currentMileage);

        log.setDoneAtMileage(request.doneAtMileage());
        log.setDoneAtDate(request.doneAtDate());
        log.setCost(request.cost());
        log.setNotes(request.notes());
        log = serviceLogRepository.save(log);

        if (request.doneAtMileage() != null && (currentMileage == null || request.doneAtMileage() > currentMileage)) {
            setVehicleMileage(vehicle, request.doneAtMileage());
        }

        return toResponse(log);
    }

    public void deleteLog(User currentUser, UUID logId) {
        ServiceLog log = getOwnedLog(currentUser, logId);
        serviceLogRepository.delete(log);
    }

    public LogDetailResponse addDetail(User currentUser, UUID logId, LogDetailRequest request) {
        ServiceLog log = getOwnedLog(currentUser, logId);

        ServiceLogDetail detail = ServiceLogDetail.builder()
                .serviceLog(log)
                .detailKey(request.key())
                .value(request.value())
                .build();

        detail = serviceLogDetailRepository.save(detail);
        return toDetailResponse(detail);
    }

    public void deleteDetail(User currentUser, UUID detailId) {
        ServiceLogDetail detail = serviceLogDetailRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceLogDetail", detailId));

        if (!detail.getServiceLog().getVehicleService().getVehicle().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException();
        }

        serviceLogDetailRepository.delete(detail);
    }

    private Integer getVehicleMileage(Vehicle vehicle) {
        return vehicle.getMileage();
    }

    private void setVehicleMileage(Vehicle vehicle, Integer mileage) {
        vehicle.setMileage(mileage);
        vehicleRepository.save(vehicle);
    }

    private void requireValidMileage(Integer doneAtMileage, Integer currentMileage) {
        if (doneAtMileage != null && currentMileage != null && doneAtMileage < currentMileage) {
            throw new InvalidServiceLogMileageException(doneAtMileage, currentMileage);
        }
    }

    private Integer computeMileageDue(VehicleService vehicleService, Integer doneMileage) {
        if (doneMileage == null || vehicleService.getIntervalMiles() == null) {
            return null;
        }
        return doneMileage + vehicleService.getIntervalMiles();
    }

    private LocalDate computeDateDue(VehicleService vehicleService, LocalDate doneDate) {
        if (vehicleService.getIntervalMonths() == null) {
            return null;
        }
        return doneDate.plusMonths(vehicleService.getIntervalMonths());
    }

    private List<ServiceLogDetail> saveDetails(ServiceLog log, List<LogDetailRequest> detailRequests) {
        if (detailRequests == null || detailRequests.isEmpty()) {
            return List.of();
        }

        List<ServiceLogDetail> details = detailRequests.stream()
                .map(detail -> ServiceLogDetail.builder()
                        .serviceLog(log)
                        .detailKey(detail.key())
                        .value(detail.value())
                        .build())
                .toList();

        return serviceLogDetailRepository.saveAll(details);
    }

    private List<ServiceLogDetail> getLogDetails(UUID logId) {
        return serviceLogDetailRepository.findAllByServiceLogId(logId);
    }

    private VehicleService validateVehicleServiceOwnership(User currentUser, UUID vehicleServiceId) {
        VehicleService vehicleService = vehicleServiceRepository.findById(vehicleServiceId)
                .orElseThrow(() -> new ResourceNotFoundException("VehicleService", vehicleServiceId));

        if (!vehicleService.getVehicle().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException();
        }

        return vehicleService;
    }

    private Vehicle getOwnedVehicle(User currentUser, UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicleId));

        if (!vehicle.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException();
        }

        return vehicle;
    }

    private ServiceLog getOwnedLog(User currentUser, UUID logId) {
        ServiceLog log = serviceLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceLog", logId));

        if (!log.getVehicleService().getVehicle().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException();
        }

        return log;
    }

    private LogResponse toResponse(ServiceLog log) {
        return toResponse(log, getLogDetails(log.getId()));
    }

    private LogResponse toResponse(ServiceLog log, List<ServiceLogDetail> details) {
        VehicleService vehicleService = log.getVehicleService();
        return new LogResponse(
                log.getId(),
                vehicleService.getId(),
                log.getDoneAtMileage(),
                computeMileageDue(vehicleService, log.getDoneAtMileage()),
                log.getDoneAtDate(),
                computeDateDue(vehicleService, log.getDoneAtDate()),
                log.getCost(),
                log.getNotes(),
                details.stream().map(this::toDetailResponse).toList());
    }

    private LogDetailResponse toDetailResponse(ServiceLogDetail detail) {
        return new LogDetailResponse(detail.getId(), detail.getDetailKey(), detail.getValue());
    }
}
