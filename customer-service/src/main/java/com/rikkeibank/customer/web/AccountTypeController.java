package com.rikkeibank.customer.web;

import com.rikkeibank.customer.domain.AccountType;
import com.rikkeibank.customer.domain.Repositories.AccountTypeRepository;
import com.rikkeibank.customer.web.error.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account-types")
public class AccountTypeController {

    private final AccountTypeRepository repo;
    public AccountTypeController(AccountTypeRepository repo) { this.repo = repo; }

    @GetMapping
    public List<AccountType> list() { return repo.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public AccountType create(@Valid @RequestBody AccountType t) { return repo.save(t); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AccountType update(@PathVariable Long id, @Valid @RequestBody AccountType body) {
        AccountType t = repo.findById(id).orElseThrow(() -> new NotFoundException("AccountType " + id));
        t.setName(body.getName());
        t.setInterestRate(body.getInterestRate());
        return repo.save(t);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        if (!repo.existsById(id)) throw new NotFoundException("AccountType " + id);
        repo.deleteById(id);
    }
}
