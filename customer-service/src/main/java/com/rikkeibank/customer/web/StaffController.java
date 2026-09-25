package com.rikkeibank.customer.web;

import com.rikkeibank.customer.domain.Staff;
import com.rikkeibank.customer.domain.Repositories.StaffRepository;
import com.rikkeibank.customer.web.error.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/staff")
@PreAuthorize("hasRole('ADMIN')")
public class StaffController {

    private final StaffRepository repo;
    public StaffController(StaffRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Staff> list() { return repo.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Staff create(@Valid @RequestBody Staff s) { return repo.save(s); }

    @PutMapping("/{id}")
    public Staff update(@PathVariable Long id, @Valid @RequestBody Staff body) {
        Staff s = repo.findById(id).orElseThrow(() -> new NotFoundException("Staff " + id));
        s.setName(body.getName());
        s.setPosition(body.getPosition());
        return repo.save(s);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repo.existsById(id)) throw new NotFoundException("Staff " + id);
        repo.deleteById(id);
    }
}
