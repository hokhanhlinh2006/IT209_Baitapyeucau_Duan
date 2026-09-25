package com.rikkeibank.account.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNo;

    @Column(nullable = false)
    private Long customerId;      // = userId của CUSTOMER (map trực tiếp trong seed demo)

    private Long typeId;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Version
    private Long version;         // optimistic lock, chống race khi cập nhật số dư

    public Long getId() { return id; }
    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String v) { this.accountNo = v; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long v) { this.customerId = v; }
    public Long getTypeId() { return typeId; }
    public void setTypeId(Long v) { this.typeId = v; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal v) { this.balance = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Long getVersion() { return version; }
}
