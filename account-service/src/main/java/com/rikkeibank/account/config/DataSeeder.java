package com.rikkeibank.account.config;

import com.rikkeibank.account.domain.Account;
import com.rikkeibank.account.domain.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seed(AccountRepository repo) {
        return args -> {
            if (repo.count() > 0) return;
            repo.save(acc("ACC001", 3L, new BigDecimal("1000000"))); // cust1 (userId 3)
            repo.save(acc("ACC002", 4L, new BigDecimal("500000")));  // cust2 (userId 4)
        };
    }
    private Account acc(String no, Long customerId, BigDecimal bal) {
        Account a = new Account();
        a.setAccountNo(no); a.setCustomerId(customerId); a.setTypeId(1L); a.setBalance(bal);
        return a;
    }
}
