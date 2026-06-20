package dev.jacksonfishburn.lubelog.exception;

import org.springframework.http.HttpStatus;

public class InvalidServiceLogMileageException extends LubeLogException {

    public InvalidServiceLogMileageException(int doneAtMileage, int currentMileage) {
        super("Service log mileage (" + doneAtMileage + ") cannot be less than the vehicle's current mileage ("
                + currentMileage + ")", HttpStatus.BAD_REQUEST);
    }
}
