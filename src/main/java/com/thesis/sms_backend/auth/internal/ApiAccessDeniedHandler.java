package com.thesis.sms_backend.auth.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thesis.sms_backend.core.ApiError;
import com.thesis.sms_backend.core.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException ex
    ) throws IOException {

        ApiResponse<Void> body = new ApiResponse<>(
                false,
                null,
                new ApiError("FORBIDDEN", "Access denied"),
                null
        );

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}