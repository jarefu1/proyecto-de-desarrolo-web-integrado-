package com.mhmstore.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.mhmstore.api.dto.ApiErrorResponse;
import com.mhmstore.api.exception.CodigoDuplicadoException;
import com.mhmstore.api.exception.ProductoNotFoundException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductoNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound(ProductoNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, Map.of());
    }

    @ExceptionHandler(CodigoDuplicadoException.class)
    public ResponseEntity<ApiErrorResponse> conflict(CodigoDuplicadoException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, "La solicitud contiene datos invalidos", req, errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> badType(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Parametro invalido: " + ex.getName(), req, Map.of());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message,
                                                    HttpServletRequest req, Map<String, String> fields) {
        ApiErrorResponse body = new ApiErrorResponse(OffsetDateTime.now(), status.value(), status.getReasonPhrase(),
                message, req.getRequestURI(), fields);
        return ResponseEntity.status(status).body(body);
    }
}
