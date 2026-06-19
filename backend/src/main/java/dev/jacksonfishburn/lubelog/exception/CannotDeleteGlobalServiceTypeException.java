package dev.jacksonfishburn.lubelog.exception;

import org.springframework.http.HttpStatus;

public class CannotDeleteGlobalServiceTypeException extends LubeLogException {

    public CannotDeleteGlobalServiceTypeException() {
        super("Global service types cannot be deleted", HttpStatus.FORBIDDEN);
    }
}
