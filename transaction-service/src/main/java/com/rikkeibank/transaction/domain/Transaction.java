package com.rikkeibank.transaction.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sagaId;

    private String fromAcc;
    private String toAcc;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Long initiatorUserId;
    private String failureReason;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status { PENDING, COMPLETED, FAILED }

    public Long getId() { return id; }
    public String getSagaId() { return sagaId; }
    public void setSagaId(String v) { this.sagaId = v; }
    public String getFromAcc() { return fromAcc; }
    public void setFromAcc(String v) { this.fromAcc = v; }
    public String getToAcc() { return toAcc; }
    public void setToAcc(String v) { this.toAcc = v; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal v) { this.amount = v; }
    public Status getStatus() { return status; }
    public void setStatus(Status v) { this.status = v; }
    public Long getInitiatorUserId() { return initiatorUserId; }
    public void setInitiatorUserId(Long v) { this.initiatorUserId = v; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String v) { this.failureReason = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
