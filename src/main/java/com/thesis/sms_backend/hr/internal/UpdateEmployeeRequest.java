package com.thesis.sms_backend.hr.internal;

import com.thesis.sms_backend.core.NotNullIfPresent;
import com.thesis.sms_backend.core.Patch;
import com.thesis.sms_backend.core.ValidEmailIfPresent;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@Builder
@Jacksonized
public class UpdateEmployeeRequest {
    @NotNullIfPresent
    @Builder.Default
    private Patch<String> firstName = Patch.absent();

    @NotNullIfPresent
    @Builder.Default
    private Patch<String> lastName = Patch.absent();

    @Builder.Default
    private Patch<String> middleName = Patch.absent();

    @NotNullIfPresent
    @ValidEmailIfPresent
    @Builder.Default
    private Patch<String> email = Patch.absent();

    @NotNullIfPresent
    @Builder.Default
    private Patch<String> phone = Patch.absent();

    @NotNullIfPresent
    @Builder.Default
    private Patch<Employee.Role> role = Patch.absent();

    @NotNullIfPresent
    @Builder.Default
    private Patch<Boolean> active = Patch.absent();
}