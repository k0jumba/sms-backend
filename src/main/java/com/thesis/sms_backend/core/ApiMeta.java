package com.thesis.sms_backend.core;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.HashMap;
import java.util.Map;

public class ApiMeta {
    private final Map<String, Object> properties = new HashMap<>();

    public ApiMeta add(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return properties;
    }
}