package com.rikkeibank.account.service;

import com.rikkeibank.account.domain.Account;
import com.rikkeibank.account.domain.AccountRepository;
import com.rikkeibank.account.web.dto.AccountDtos.AccountView;
import com.rikkeibank.account.web.error.ApiException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository repo;
    public AccountService(AccountRepository repo) { this.repo = repo; }

    public List<AccountView> byCustomer(Long customerId) {
        return repo.findByCustomerId(customerId).stream().map(this::toView).toList();
    }

    // Cache-Aside: đọc số dư qua @Cacheable, key = accountNo
    @Cacheable(value = "accounts", key = "#accountNo")
    public AccountView getByAccountNo(String accountNo) {
        return toView(load(accountNo));
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#accountNo")
    public AccountView debit(String accountNo, BigDecimal amount) {
        Account a = load(accountNo);
        if (a.getBalance().compareTo(amount) < 0) {
            throw new ApiException(HttpStatus.CONFLICT, "Số dư không đủ ở " + accountNo);
        }
        a.setBalance(a.getBalance().subtract(amount));
        return toView(repo.save(a));
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#accountNo")
    public AccountView credit(String accountNo, BigDecimal amount) {
        Account a = load(accountNo);
        a.setBalance(a.getBalance().add(amount));
        return toView(repo.save(a));
    }

    private Account load(String accountNo) {
        return repo.findByAccountNo(accountNo)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tài khoản " + accountNo));
    }

    private AccountView toView(Account a) {
        return new AccountView(a.getAccountNo(), a.getCustomerId(), a.getBalance(), a.getStatus());
    }
}
