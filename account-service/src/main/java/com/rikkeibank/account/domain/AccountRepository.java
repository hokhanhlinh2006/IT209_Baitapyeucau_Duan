package com.rikkeibank.account.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNo(String accountNo);
    List<Account> findByCustomerId(Long customerId);
}
