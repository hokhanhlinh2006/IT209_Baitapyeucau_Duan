package com.rikkeibank.customer.config;

import com.rikkeibank.customer.domain.*;
import com.rikkeibank.customer.domain.Repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seed(CustomerRepository customers, AccountTypeRepository types, StaffRepository staff) {
        return args -> {
            if (customers.count() == 0) {
                Customer c1 = new Customer(); c1.setFullName("Nguyen Van A"); c1.setEmail("a@example.com");
                c1.setPhone("0900000001"); c1.setNationalId("0790001");
                Customer c2 = new Customer(); c2.setFullName("Tran Thi B"); c2.setEmail("b@example.com");
                c2.setPhone("0900000002"); c2.setNationalId("0790002");
                customers.save(c1); customers.save(c2);
            }
            if (types.count() == 0) {
                AccountType t1 = new AccountType(); t1.setName("Thanh toan"); t1.setInterestRate(new BigDecimal("0.001"));
                AccountType t2 = new AccountType(); t2.setName("Tiet kiem"); t2.setInterestRate(new BigDecimal("0.045"));
                types.save(t1); types.save(t2);
            }
            if (staff.count() == 0) {
                Staff s = new Staff(); s.setName("Le Van Teller"); s.setPosition("TELLER");
                staff.save(s);
            }
        };
    }
}
