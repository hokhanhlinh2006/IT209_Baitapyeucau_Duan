package com.rikkeibank.transaction.messaging;

import java.math.BigDecimal;

public record TransferEvent(
        String sagaId, Long txId, String fromAcc, String toAcc,
        BigDecimal amount, String status) {}
