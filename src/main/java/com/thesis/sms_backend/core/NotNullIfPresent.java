package com.thesis.sms_backend.core;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = NotNullIfPresentValidator.class)
public @interface NotNullIfPresent {
    String message() default "Field cannot be null";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}