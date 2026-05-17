package com.thesis.sms_backend.auth.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(

        @NotBlank
        String login,

        @NotBlank
        String password,

        @NotNull
        User.Role role,

        @NotNull
        Boolean active
) {}