package com.thesis.sms_backend.auth.internal;

import com.thesis.sms_backend.core.NotNullIfPresent;
import com.thesis.sms_backend.core.Patch;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@Builder
@Jacksonized
public class UpdateUserRequest {
    @NotNullIfPresent
    @Builder.Default
    private Patch<String> login = Patch.absent();

    @NotNullIfPresent
    @Builder.Default
    private Patch<String> password = Patch.absent();

    @NotNullIfPresent
    @Builder.Default
    private Patch<User.Role> role = Patch.absent();

    @NotNullIfPresent
    @Builder.Default
    private Patch<Boolean> active = Patch.absent();
}
