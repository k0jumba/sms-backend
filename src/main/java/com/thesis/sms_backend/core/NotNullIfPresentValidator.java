package com.thesis.sms_backend.core;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotNullIfPresentValidator implements ConstraintValidator<NotNullIfPresent, Patch<?>> {
    @Override
    public boolean isValid(Patch<?> patch, ConstraintValidatorContext ctx) {
        return !patch.isPresent() || patch.getValue() != null;
    }
}