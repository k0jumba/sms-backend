package com.thesis.sms_backend.core;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonPatchConfig {

    @Bean
    public SimpleModule patchModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Patch.class, new PatchDeserializer());
        return module;
    }
}