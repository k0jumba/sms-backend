package com.thesis.sms_backend;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;


@Configuration
public class FlywayConfig {

    @Bean(initMethod = "migrate", name = "defaultFlyway")
    public Flyway defaultFlyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/default")
                .table("flyway_schema_history")
                .load();
    }
}