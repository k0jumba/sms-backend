package com.thesis.sms_backend.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.function.Consumer;

@Getter
public final class Patch<T> {

    private static final Patch<?> ABSENT = new Patch<>(null, false);

    private final T value;
    private final boolean present;

    private Patch(T value, boolean present) {
        this.value = value;
        this.present = present;
    }

    @JsonCreator
    public static <T> Patch<T> of(T value) {
        return new Patch<>(value, true);
    }

    @SuppressWarnings("unchecked")
    public static <T> Patch<T> absent() {
        return (Patch<T>) ABSENT;
    }

    public void ifPresent(Consumer<T> setter) {
        if (present) setter.accept(value);
    }
}
