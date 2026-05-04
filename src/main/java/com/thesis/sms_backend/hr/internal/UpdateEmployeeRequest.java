package com.thesis.sms_backend.hr.internal;

import com.thesis.sms_backend.core.NotNullIfPresent;
import com.thesis.sms_backend.core.Patch;
import com.thesis.sms_backend.core.ValidEmailIfPresent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEmployeeRequest {
    @NotNullIfPresent
    private Patch<String> firstName = Patch.absent();
    @NotNullIfPresent
    private Patch<String> lastName = Patch.absent();
    private Patch<String> middleName = Patch.absent();
    @NotNullIfPresent
    @ValidEmailIfPresent
    private Patch<String> email = Patch.absent();
    private Patch<String> phone = Patch.absent();
    @NotNullIfPresent
    private Patch<Employee.Role> role = Patch.absent();
    @NotNullIfPresent
    private Patch<Boolean> active = Patch.absent();
}