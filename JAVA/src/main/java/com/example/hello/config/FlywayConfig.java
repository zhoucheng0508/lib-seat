package com.example.hello.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {
    
    @Bean
    public Flyway flyway() {
        Flyway flyway = Flyway.configure()
                .dataSource("jdbc:mysql://localhost:3306/lib_seat?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=utf8", 
                          "root", "9nc44sl7")
                .baselineOnMigrate(true)
                .locations("classpath:db/migration")
                .table("flyway_schema_history")
                .validateOnMigrate(true)
                .load();
        return flyway;
    }
} 