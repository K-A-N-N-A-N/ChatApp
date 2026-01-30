package com.example.ChatApp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleRuntime(RuntimeException ex) {
        return Map.of(
                "timestamp", Instant.now(),
                // Map.of doesn't allow null values; keep this non-null.
                "error", ex.getMessage() == null ? "Runtime error" : ex.getMessage()
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Map<String, Object> handleStatus(ResponseStatusException ex) {
        return Map.of(
                "timestamp", Instant.now(),
                "status", ex.getStatusCode().value(),
                "error", ex.getReason() == null ? ex.getMessage() : ex.getReason()
        );
    }
}
