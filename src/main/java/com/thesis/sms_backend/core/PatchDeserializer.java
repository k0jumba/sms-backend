package com.thesis.sms_backend.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

public class PatchDeserializer extends JsonDeserializer<Patch<?>>
        implements ContextualDeserializer {

    private JavaType valueType;

    public PatchDeserializer() {}

    private PatchDeserializer(JavaType valueType) {
        this.valueType = valueType;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctx, BeanProperty property) {
        JavaType innerType = property.getType().containedType(0);
        return new PatchDeserializer(innerType);
    }

    @Override
    public Patch<?> deserialize(JsonParser p, DeserializationContext ctx) throws java.io.IOException {
        Object value = ctx.readValue(p, valueType);
        return Patch.of(value);
    }

    @Override
    public Patch<?> getNullValue(DeserializationContext ctx) {
        return Patch.of(null);
    }
}