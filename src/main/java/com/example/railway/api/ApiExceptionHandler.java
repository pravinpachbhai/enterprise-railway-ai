package com.example.railway.api;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String,Object>> notFound(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", safe(e)));
    }
    @ExceptionHandler(org.springframework.dao.EmptyResultDataAccessException.class)
    public ResponseEntity<Map<String,Object>> missing(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Record not found"));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,Object>> badRequest(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", safe(e)));
    }
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String,Object>> conflict(Exception e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", safe(e)));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> invalid(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid request", "details", e.getBindingResult().getFieldErrors().stream()
                .map(x -> x.getField() + ": " + x.getDefaultMessage()).toList()));
    }
    private String safe(Exception e) { return e.getMessage() == null ? "Request failed" : e.getMessage(); }
}
