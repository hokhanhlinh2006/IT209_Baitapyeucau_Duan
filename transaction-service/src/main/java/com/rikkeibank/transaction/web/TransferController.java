package com.rikkeibank.transaction.web;

import com.rikkeibank.transaction.domain.Transaction;
import com.rikkeibank.transaction.domain.TransactionRepository;
import com.rikkeibank.transaction.saga.TransferSaga;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
public class TransferController {

    private final TransferSaga saga;
    private final TransactionRepository repo;

    public TransferController(TransferSaga saga, TransactionRepository repo) {
        this.saga = saga; this.repo = repo;
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    public Transaction transfer(@Valid @RequestBody TransferRequest req,
                                @RequestHeader("X-User-Id") Long userId) {
        return saga.transfer(req.fromAcc(), req.toAcc(), req.amount(), userId);
    }

    @GetMapping("/transactions/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<Transaction> mine(@RequestHeader("X-User-Id") Long userId) {
        return repo.findByInitiatorUserIdOrderByCreatedAtDesc(userId);
    }

    @GetMapping("/transactions/today")
    @PreAuthorize("hasAnyRole('TELLER','ADMIN')")
    public List<Transaction> today() {
        return repo.findByCreatedAtBetweenOrderByCreatedAtDesc(
                LocalDate.now().atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX));
    }

    public record TransferRequest(
            @NotBlank String fromAcc,
            @NotBlank String toAcc,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount) {}
}
