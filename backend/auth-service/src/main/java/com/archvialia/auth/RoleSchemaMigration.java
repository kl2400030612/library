package com.archvialia.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Keeps databases created by older Archvialia versions compatible with the
 * current Student/Faculty/Admin role model. Hibernate's schema update does not
 * reliably widen an existing PostgreSQL CHECK constraint, so this small,
 * idempotent migration does it explicitly at startup.
 */
@Component
@Order(0)
public class RoleSchemaMigration implements CommandLineRunner {
    private final JdbcTemplate jdbc;

    public RoleSchemaMigration(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        jdbc.execute("ALTER TABLE auth_users DROP CONSTRAINT IF EXISTS auth_users_role_check");
        jdbc.execute("ALTER TABLE auth_users ADD CONSTRAINT auth_users_role_check CHECK (role IN ('USER','STUDENT','FACULTY','ADMIN'))");
    }
}
