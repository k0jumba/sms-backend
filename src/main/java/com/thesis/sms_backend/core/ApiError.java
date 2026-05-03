package com.thesis.sms_backend.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private String code;
    private String message;
    private List<FieldViolation> fields;

    public ApiError(String code, String message) {
        this(code, message, null);
    }

    @Data
    @AllArgsConstructor
    public static class FieldViolation {
        private String field;
        private String message;
    }
}
