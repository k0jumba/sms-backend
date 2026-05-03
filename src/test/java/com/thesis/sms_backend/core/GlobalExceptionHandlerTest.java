package com.thesis.sms_backend.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void genericException_returnsStandard500() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleGeneric(new RuntimeException("fail"));

        ApiResponse<Void> body = response.getBody();

        Assertions.assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("INTERNAL_ERROR", body.getError().getCode());
    }
}