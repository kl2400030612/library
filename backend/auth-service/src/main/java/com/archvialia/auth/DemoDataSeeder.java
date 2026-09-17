package com.archvialia.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Order(1)
public class DemoDataSeeder {
    @Bean
    CommandLineRunner seed(UserRepository repo, PasswordEncoder encoder,
                           @Value("${app.seed-demo-data:false}") boolean enabled,
                           @Value("${app.seed-admin-email:admin@archvialia.local}") String adminEmail,
                           @Value("${app.seed-admin-password:Admin@12345}") String adminPassword,
                           @Value("${app.seed-user-email:user@archvialia.local}") String userEmail,
                           @Value("${app.seed-user-password:User@12345}") String userPassword) {
        return args -> {
            if (!enabled) return;
            if (!repo.existsByEmailIgnoreCase(adminEmail)) {
                User u = new User(); u.setName("Archvialia Admin"); u.setEmail(adminEmail.toLowerCase()); u.setPasswordHash(encoder.encode(adminPassword)); u.setRole(Role.ADMIN); repo.save(u);
            }
            if (!repo.existsByEmailIgnoreCase(userEmail)) {
                User u = new User(); u.setName("Demo Student"); u.setEmail(userEmail.toLowerCase()); u.setPasswordHash(encoder.encode(userPassword)); u.setRole(Role.STUDENT); repo.save(u);
            }
        };
    }
}
