package com.rikkeibank.account.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public class AccountDtos {
    // Serializable + fields đơn giản để cache JSON qua Redis an toàn
    public record AccountView(String accountNo, Long customerId, BigDecimal balance, String status)
            implements Serializable {}

    public record AmountRequest(@NotNull @DecimalMin(value = "0.01") BigDecimal amount) {}
}
