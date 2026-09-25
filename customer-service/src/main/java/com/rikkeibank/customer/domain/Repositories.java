package com.rikkeibank.customer.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public class Repositories {
    public interface CustomerRepository extends JpaRepository<Customer, Long> {}
    public interface StaffRepository extends JpaRepository<Staff, Long> {}
    public interface AccountTypeRepository extends JpaRepository<AccountType, Long> {}
}
