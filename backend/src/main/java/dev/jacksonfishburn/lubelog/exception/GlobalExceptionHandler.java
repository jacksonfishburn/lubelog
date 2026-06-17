package dev.jacksonfishburn.lubelog.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LubeLogException.class)
    public ResponseEntity<ErrorResponse> handleLubeLogException(LubeLogException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ErrorResponse(ex.getStatus().value(), ex.getMessage()));
    }

    public record ErrorResponse(int status, String message) {}
}
