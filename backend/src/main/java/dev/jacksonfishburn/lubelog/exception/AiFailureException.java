package dev.jacksonfishburn.lubelog.exception;

import org.springframework.http.HttpStatus;

public class AiFailureException extends LubeLogException {
    public AiFailureException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
