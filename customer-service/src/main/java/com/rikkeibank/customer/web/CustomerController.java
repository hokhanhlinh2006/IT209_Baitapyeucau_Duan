package com.rikkeibank.customer.web;

import com.rikkeibank.customer.domain.Customer;
import com.rikkeibank.customer.domain.Repositories.CustomerRepository;
import com.rikkeibank.customer.web.error.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerRepository repo;
    public CustomerController(CustomerRepository repo) { this.repo = repo; }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TELLER')")
    public List<Customer> list() { return repo.findAll(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TELLER')")
    public Customer get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Customer " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Customer create(@Valid @RequestBody Customer c) { return repo.save(c); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Customer update(@PathVariable Long id, @Valid @RequestBody Customer body) {
        Customer c = repo.findById(id).orElseThrow(() -> new NotFoundException("Customer " + id));
        c.setFullName(body.getFullName());
        c.setEmail(body.getEmail());
        c.setPhone(body.getPhone());
        c.setNationalId(body.getNationalId());
        return repo.save(c);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        if (!repo.existsById(id)) throw new NotFoundException("Customer " + id);
        repo.deleteById(id);
    }
}
