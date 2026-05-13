package com.thesis.sms_backend.core;

import lombok.Getter;

@Getter
public class UniqueConstraintViolationException extends RuntimeException {

    private final String field;
    private final Object value;

    public UniqueConstraintViolationException(String field, Object value) {
        super("Value already exists: " + field);
        this.field = field;
        this.value = value;
    }

}