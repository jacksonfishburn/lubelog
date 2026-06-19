package dev.jacksonfishburn.lubelog.exception;

import org.springframework.http.HttpStatus;

public class DuplicateServiceTypeException extends LubeLogException {

    public DuplicateServiceTypeException(String name) {
        super("A service type with the name '" + name + "' already exists", HttpStatus.CONFLICT);
    }
}
