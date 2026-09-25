package com.rikkeibank.account.web;

import com.rikkeibank.account.service.AccountService;
import com.rikkeibank.account.web.dto.AccountDtos.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService service;
    public AccountController(AccountService service) { this.service = service; }

    // CUSTOMER xem tài khoản của chính mình (customerId = userId)
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<AccountView> myAccounts(@RequestHeader("X-User-Id") Long userId) {
        return service.byCustomer(userId);
    }

    @GetMapping("/{accountNo}")
    @PreAuthorize("hasAnyRole('CUSTOMER','TELLER','ADMIN')")
    public AccountView get(@PathVariable String accountNo) {
        return service.getByAccountNo(accountNo);
    }

    // Endpoint NỘI BỘ: transaction-service gọi trực tiếp qua Feign (không qua gateway)
    @PostMapping("/{accountNo}/debit")
    public AccountView debit(@PathVariable String accountNo, @Valid @RequestBody AmountRequest req) {
        return service.debit(accountNo, req.amount());
    }

    @PostMapping("/{accountNo}/credit")
    public AccountView credit(@PathVariable String accountNo, @Valid @RequestBody AmountRequest req) {
        return service.credit(accountNo, req.amount());
    }
}
