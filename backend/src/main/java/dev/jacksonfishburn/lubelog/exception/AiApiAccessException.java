package dev.jacksonfishburn.lubelog.exception;

import org.springframework.http.HttpStatus;

public class AiApiAccessException extends LubeLogException {
    public AiApiAccessException(String message) {
        super(message, HttpStatus.BAD_GATEWAY);
    }
}
