package dev.jacksonfishburn.lubelog.exception;

import org.springframework.http.HttpStatus;

public class VinLookupException extends LubeLogException {

    public VinLookupException(String message) {
        super(message, HttpStatus.BAD_GATEWAY);
    }
}
