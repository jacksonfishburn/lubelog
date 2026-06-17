package dev.jacksonfishburn.lubelog.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends LubeLogException {

    public AccessDeniedException() {
        super("You do not have permission to access this resource", HttpStatus.FORBIDDEN);
    }
}
