package com.thesis.sms_backend.hr.internal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateEmployeeRequest(

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        String middleName,

        @NotNull
        Employee.Role role,

        @NotNull
        Boolean active,

        @NotBlank
        @Email
        String email,

        @NotBlank
        String phone
) {}