package com.rikkeibank.identity.config;

import com.rikkeibank.identity.domain.Role;
import com.rikkeibank.identity.domain.User;
import com.rikkeibank.identity.domain.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seed(UserRepository repo, PasswordEncoder enc) {
        return args -> {
            if (repo.count() > 0) return;
            repo.save(user("admin", "admin123", Role.ADMIN, enc));       // id 1
            repo.save(user("teller1", "teller123", Role.TELLER, enc));   // id 2
            repo.save(user("cust1", "cust123", Role.CUSTOMER, enc));     // id 3 -> ACC001
            repo.save(user("cust2", "cust123", Role.CUSTOMER, enc));     // id 4 -> ACC002
        };
    }
    private User user(String u, String p, Role r, PasswordEncoder enc) {
        User x = new User();
        x.setUsername(u); x.setPassword(enc.encode(p)); x.setRole(r);
        return x;
    }
}
