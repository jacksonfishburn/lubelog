package dev.jacksonfishburn.lubelog.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends LubeLogException {

    public InvalidRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
