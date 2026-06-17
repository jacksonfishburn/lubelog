package dev.jacksonfishburn.lubelog.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends LubeLogException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super(resourceName + " not found with id: " + id, HttpStatus.NOT_FOUND);
    }
}
