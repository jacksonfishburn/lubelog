package dev.jacksonfishburn.lubelog.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public abstract class LubeLogException extends RuntimeException {

    private final HttpStatus status;

    protected LubeLogException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
