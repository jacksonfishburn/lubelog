package dev.jacksonfishburn.lubelog.exception;

import org.springframework.http.HttpStatus;

public class InvalidMileageException extends LubeLogException {

    public InvalidMileageException(int newMileage, int currentHighWaterMark) {
        super("New mileage (" + newMileage + ") cannot be less than the highest recorded service mileage ("
                + currentHighWaterMark + ")", HttpStatus.BAD_REQUEST);
    }
}
