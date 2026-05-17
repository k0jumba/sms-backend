package com.thesis.sms_backend.auth.internal;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

@Configuration
public class AuthFlywayConfig {

    @Bean(initMethod = "migrate")
    @DependsOn("defaultFlyway")
    public Flyway authFlyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .schemas("auth")
                .locations("classpath:db/migration/auth")
                .table("flyway_auth_schema_history")
                .load();
    }
}