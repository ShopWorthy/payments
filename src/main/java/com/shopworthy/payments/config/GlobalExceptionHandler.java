package com.shopworthy.payments.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", ex.getMessage());
        error.put("stack", Arrays.toString(ex.getStackTrace()));  // Full stack in response
        error.put("cause", ex.getCause() != null ? ex.getCause().getMessage() : null);
        return ResponseEntity.status(500).body(error);
    }
}
