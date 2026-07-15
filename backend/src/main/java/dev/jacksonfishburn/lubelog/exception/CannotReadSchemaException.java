package dev.jacksonfishburn.lubelog.exception;

import org.springframework.http.HttpStatus;

public class CannotReadSchemaException extends LubeLogException {
    public CannotReadSchemaException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
