package com.thesis.sms_backend.hr.internal;

import jakarta.validation.constraints.Email;

public record UpdateEmployeeRequest(

        String firstName,
        String lastName,
        String middleName,
        Employee.Role role,
        Boolean active,

        @Email
        String email,

        String phone
) {}