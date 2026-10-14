
package com.mhmstore.api.controller;

import com.mhmstore.api.dto.ApiErrorResponse;
import com.mhmstore.api.exception.CodigoDuplicadoException;
import com.mhmstore.api.exception.ProductoNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductoNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound(
            ProductoNotFoundException ex,
            HttpServletRequest req) {

        return build(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                req,
                Map.of()
        );
    }

    @ExceptionHandler(CodigoDuplicadoException.class)
    public ResponseEntity<ApiErrorResponse> conflict(
            CodigoDuplicadoException ex,
            HttpServletRequest req) {

        return build(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                req,
                Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> validation(
            MethodArgumentNotValidException ex,
            HttpServletRequest req) {

        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage()
                )
        );

        return build(
                HttpStatus.BAD_REQUEST,
                "La solicitud contiene datos inválidos",
                req,
                errors
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> badType(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest req) {

        return build(
                HttpStatus.BAD_REQUEST,
                "Parámetro inválido: " + ex.getName(),
                req,
                Map.of()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> badJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest req) {

        return build(
                HttpStatus.BAD_REQUEST,
                "El JSON está mal formado o contiene tipos de datos incorrectos",
                req,
                Map.of()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> constraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest req) {

        return build(
                HttpStatus.BAD_REQUEST,
                "Los datos enviados no cumplen las restricciones",
                req,
                Map.of()
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> statusException(
            ResponseStatusException ex,
            HttpServletRequest req) {

        HttpStatus status = HttpStatus.valueOf(
                ex.getStatusCode().value()
        );

        String message = ex.getReason() != null
                ? ex.getReason()
                : status.getReasonPhrase();

        return build(status, message, req, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> dataConflict(
            DataIntegrityViolationException ex,
            HttpServletRequest req) {

        return build(
                HttpStatus.CONFLICT,
                "La operación entra en conflicto con los datos existentes "
                        + "o con relaciones de la base de datos",
                req,
                Map.of()
        );
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String message,
            HttpServletRequest req,
            Map<String, String> fields) {

        ApiErrorResponse body = new ApiErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                req.getRequestURI(),
                fields
        );

        return ResponseEntity.status(status).body(body);
    }
}
