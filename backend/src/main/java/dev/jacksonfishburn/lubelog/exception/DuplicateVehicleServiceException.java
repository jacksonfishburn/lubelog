package dev.jacksonfishburn.lubelog.exception;

import org.springframework.http.HttpStatus;

public class DuplicateVehicleServiceException extends LubeLogException {

    public DuplicateVehicleServiceException(String serviceTypeName) {
        super("This vehicle already has '" + serviceTypeName + "' activated", HttpStatus.CONFLICT);
    }
}
