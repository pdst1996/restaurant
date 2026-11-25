
package com.ps.restaurant.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.stereotype.Component;

@Component
public class DatabaseFunctionInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseFunctionInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String database = jdbcTemplate.execute((ConnectionCallback<String>)
                connection -> connection.getMetaData().getDatabaseProductName());
        if (!"PostgreSQL".equals(database)) {
            return;
        }
        jdbcTemplate.execute("""
            CREATE OR REPLACE FUNCTION jsonb_contains_value(
                data JSONB,
                search_value TEXT
            )
            RETURNS BOOLEAN
            LANGUAGE SQL
            IMMUTABLE
            AS $$
                SELECT EXISTS (
                    SELECT 1
                    FROM jsonb_each_text(data) AS entry(key, value)
                    WHERE entry.value = search_value
                );
            $$;
            """);
    }
}
