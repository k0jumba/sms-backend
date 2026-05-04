package com.thesis.sms_backend.core;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null,
                        new ApiError("BAD_REQUEST", message),
                        null));
    }

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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleNotReadable(HttpMessageNotReadableException ex) {
        ApiError error;

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife && !ife.getPath().isEmpty()) {
            String field = ife.getPath().getFirst().getFieldName();
            String value = String.valueOf(ife.getValue());
            error = new ApiError("BAD_REQUEST",
                    String.format("Invalid value '%s' for field '%s'", value, field));
        } else {
            error = new ApiError("BAD_REQUEST", "Malformed or unreadable request body");
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, null, error, null));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolation(ConstraintViolationException ex) {
        List<ApiError.FieldViolation> fields = ex.getConstraintViolations().stream()
                .map(cv -> {
                    String field = cv.getPropertyPath().toString();
                    field = field.contains(".") ? field.substring(field.lastIndexOf('.') + 1) : field;
                    return new ApiError.FieldViolation(field, cv.getMessage());
                })
                .toList();

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiResponse<>(false, null,
                        new ApiError("UNPROCESSABLE_ENTITY", "Invalid request parameters", fields),
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
        log.error("Unexpected error", ex);

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