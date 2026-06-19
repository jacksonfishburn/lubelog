package dev.jacksonfishburn.lubelog.exception;

import org.springframework.http.HttpStatus;

public class InvalidServiceIntervalException extends LubeLogException {

    public InvalidServiceIntervalException() {
        super("At least one of intervalMiles or intervalMonths must be provided", HttpStatus.BAD_REQUEST);
    }
}
