package com.thesis.sms_backend.core;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ApiResponse<>(false, null, new ApiError("METHOD_NOT_ALLOWED", ex.getMessage()), null));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, null,
                        new ApiError("NOT_FOUND", "Resource not found"),
                        null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException ex) {

        List<ApiError.FieldViolation> fields = new ArrayList<>();

        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fields.add(new ApiError.FieldViolation(fe.getField(), fe.getDefaultMessage()))
        );

        ex.getBindingResult().getGlobalErrors().forEach(ge ->
                fields.add(new ApiError.FieldViolation(null, ge.getDefaultMessage()))
        );

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiResponse<>(false, null,
                        new ApiError("UNPROCESSABLE_ENTITY", "Method argument not valid", fields),
                        null));
    }

    @ExceptionHandler(UniqueConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleUniqueConstraint(UniqueConstraintViolationException ex) {
        List<ApiError.FieldViolation> fields = List.of(
                new ApiError.FieldViolation(ex.getField(), "Value already exists")
        );
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(false, null,
                        new ApiError("CONFLICT", "Unique constraint violation", fields),
                        null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {

        ApiResponse<Void> body = new ApiResponse<>(
                false,
                null,
                new ApiError("INTERNAL_ERROR", "Unexpected error"),
                null
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }
}