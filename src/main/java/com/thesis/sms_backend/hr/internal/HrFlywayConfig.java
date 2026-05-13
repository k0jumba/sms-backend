package com.thesis.sms_backend.hr.internal;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

@Configuration
public class HrFlywayConfig {

    @Bean(initMethod = "migrate")
    @DependsOn("defaultFlyway")
    public Flyway hrFlyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .schemas("hr")
                .locations("classpath:db/migration/hr")
                .table("flyway_hr_schema_history")
                .load();
    }
}